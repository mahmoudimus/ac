package junit.all;

import com.atlassian.mail.Email;
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil;
import com.dumbster.smtp.SimpleSmtpServer;
import junit.MailUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.ServiceAccessor;

import javax.mail.MessagingException;
import java.io.IOException;

import static java.util.Arrays.asList;

public class SendEmailTest
{
    private SimpleSmtpServer mailServer;

    @Before
    public void startMailServer() throws IOException, InterruptedException
    {
        // starting it this way as we don't want to wait for a open socket
        mailServer = new SimpleSmtpServer(2525);
        Thread t = new Thread(mailServer);
        t.start();
        while (mailServer.isStopped())
        {
            Thread.sleep(100);
        }
        ServiceAccessor.getEmailSender().flush();
        mailServer.stop();

        // starting it this way as we don't want to wait for a open socket
        mailServer = new SimpleSmtpServer(2525);
        t = new Thread(mailServer);
        t.start();
        while (mailServer.isStopped())
        {
            Thread.sleep(100);
        }

    }
    @After
    public void stopMailServer()
    {
        mailServer.stop();
    }

    @Test
    public void testSendEmail() throws Exception
    {
        Email email = new Email("betty")
                .setFrom("admin@example.com")
                .setSubject("Hi");
        ServiceAccessor.getEmailSender().send("betty", email, "bodyHtml", "bodyText");
        ServiceAccessor.getEmailSender().flush();
        MailUtils.assertEmailExists(mailServer, "betty@example.com", "Hi", asList
                            ("body"));
    }
}
