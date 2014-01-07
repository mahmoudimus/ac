package com.atlassian.plugin.connect.plugin.module.webfragment;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Note: This class is an abomination brought over from the seventh level of hell (aka ACDEV-397 branch).
 * The path to salvation is via ACDEV-498
 * ---- snip ----
 *
 * Helper to create the various forms we need the parameters in. It's functions are:
 * Converts between a map of individual params in path expression form like
 * [project.id -> 10; project.key -> "foo"]
 * and a nested map of params in json style form like
 * [project -> [id -> 10; key -> "foo"]]
 * <p/>
 * Note: as part of including the fix in AC-758 the context variables in the request are no longer the template variables
 * but rather the parameter key in the plugin xml. e.g. if plugin xml had a url like ...?myprojectid=${project.id} then
 * the request has ?myprojectid=1234 instead of ?project.id=1234 as it previously had. So first we swap those variables
 * back to the latter form
 */
public class HideousParameterContextThingy
{
    private static final Joiner PATH_COMPONENT_JOINER = Joiner.on('.');


    public static Map<String, String[]> castToStringArrays(Map<String, Object> params)
    {
        return Maps.transformValues(params, new Function<Object, String[]>()
        {
            @Override
            public String[] apply(@Nullable Object input)
            {
                return (String[]) input;
            }
        });
    }

    public static Map<String, Object> transformToNestedMapWithCast(Map<String, Object> paramsInPathForm)
    {
        return transformToNestedMap(castToStringArrays(paramsInPathForm));
    }

    public static Map<String, Object> transformToNestedMap(Map<String, String[]> paramsInPathForm)
    {
        /*
          We process by splitting params like foo.bar.id = 10 & foo.blah = 6
          into a list of pairs like ([foo, bar, id], 10) & ([foo, blah], 6)
          then convert into a multimap like foo -> {([bar, id], 10) & ([blah], 6)}
          and then recurse
         */
        final ImmutableList.Builder<Pair<List<String>, String[]>> builder = ImmutableList.builder();
        for (Map.Entry<String, String[]> entry : paramsInPathForm.entrySet())
        {
            builder.add(Pair.of(Arrays.asList(entry.getKey().split("\\.")), entry.getValue()));
        }

        return transformToNestedMap(builder.build());
    }

    private static Map<String, Object> transformToNestedMap(Iterable<Pair<List<String>, String[]>> pairs)
    {
        final ImmutableMultimap.Builder<String, Pair<List<String>, String[]>> builder =
                ImmutableMultimap.builder();
        for (Pair<List<String>, String[]> pair : pairs)
        {
            final List<String> pathComponents = pair.getLeft();
            final String[] value = pair.getRight();
            if (pathComponents.isEmpty())
            {
                continue; // TODO: Should we throw error? This would happen if they passed something like foo.bar = 10 & foo = 6
//                throw new MalformedRequestException("TODO");
            }
            final String key = pathComponents.get(0);
            final List<String> remainingPathComponents = pathComponents.subList(1, pathComponents.size());

            builder.put(key, Pair.of(remainingPathComponents, value));
        }

        return Maps.transformValues(builder.build().asMap(), new Function<Collection<Pair<List<String>, String[]>>, Object>()
        {
            @Override
            public Object apply(@Nullable Collection<Pair<List<String>, String[]>> input)
            {
                return transformPathTermValuePairs(input);
            }
        });

    }

    private static Object transformPathTermValuePairs(Iterable<Pair<List<String>, String[]>> pairs)
    {
        if (Iterables.size(pairs) == 1)
        {
            final Pair<List<String>, String[]> pair = Iterables.getFirst(pairs, null);
            final List<String> pathComponents = pair.getLeft();
            final String[] value = pair.getRight();
            if (pathComponents.isEmpty())
            {
                return isEmpty(value) ? "" : value[0]; // TODO: assuming only one value. Is this valid for us?;
            }
        }

        return transformToNestedMap(pairs);
    }


    public static Map<String, String[]> transformToPathForm(Map<String, Object> nestedMapsParams)
    {
        final ImmutableMap.Builder<String, String[]> builder = ImmutableMap.builder();
        final Iterable<Pair<List<String>, String[]>> pairs = transformToPathFormPairs(nestedMapsParams);
        for (Pair<List<String>, String[]> pair : pairs)
        {
            builder.put(createPath(pair.getLeft()), pair.getRight());
        }

        return builder.build();
    }

    private static String createPath(List<String> pathComponents)
    {
        return PATH_COMPONENT_JOINER.join(pathComponents);
    }

    private static Iterable<Pair<List<String>, String[]>> transformToPathFormPairs(Map<?, ?> nestedMapsParams)
    {
        final ImmutableList.Builder<Pair<List<String>, String[]>> resultBuilder = ImmutableList.builder();

        for (final Map.Entry<?, ?> entry : nestedMapsParams.entrySet())
        {
            Iterable<Pair<List<String>, String[]>> pairs = transformNestedValue(entry.getValue());
            // prepend our key to list of path components
            final Iterable<Pair<List<String>, String[]>> result =
                    Iterables.transform(pairs, new Function<Pair<List<String>, String[]>, Pair<List<String>, String[]>>()
                    {
                        @Override
                        public Pair<List<String>, String[]> apply(@Nullable Pair<List<String>, String[]> pair)
                        {
                            checkNotNull(pair);
                            final ImmutableList.Builder<String> builder = ImmutableList.builder();
                            builder.add(ObjectUtils.toString(entry.getKey()));
                            builder.addAll(pair.getLeft());
                            return Pair.of((List<String>) builder.build(), pair.getValue());
                        }
                    });

            resultBuilder.addAll(result);
        }


        return resultBuilder.build();
    }

    private static Iterable<Pair<List<String>, String[]>> transformNestedValue(Object value)
    {
        if (value instanceof Map)
            return transformToPathFormPairs((Map<?, ?>) value);

        String[] arrValue = value instanceof String[] ? (String[]) value : new String[]{value.toString()};

        return ImmutableList.of(Pair.of((List<String>) ImmutableList.<String>of(), arrValue));
    }

}
