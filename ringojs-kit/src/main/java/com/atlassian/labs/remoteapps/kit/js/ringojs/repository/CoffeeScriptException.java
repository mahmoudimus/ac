package com.atlassian.labs.remoteapps.kit.js.ringojs.repository;

/**
 * Copyright 2011 Mark Derricutt.
 *
 * Contributing authors:
 *   Daniel Bower
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Class to wrap Javascript CoffeeScript Compiler Errors
 *
 */
public class CoffeeScriptException extends RuntimeException {
    private static final long serialVersionUID = 2449069058134279751L;

	public CoffeeScriptException(String message) {
        super(message);
    }

	public CoffeeScriptException(String message, Throwable t) {
        super(message, t);
    }
}
