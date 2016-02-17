package it.jira.util;

import java.io.IOException;
import java.net.BindException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.email.OutgoingMailHelper;
import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.atlassian.jira.webtests.JIRAServerSetup;
import com.atlassian.jira.webtests.util.mail.MailService;
import org.apache.commons.io.IOUtils;

public class MailTestHelper
{
    private final Backdoor backdoor;
    private final MailService mailService;
    private final OutgoingMailHelper mailHelper;

    public MailTestHelper(final Backdoor backdoor)
    {
        this.backdoor = backdoor;
        this.mailService = new MailService(new FuncTestLoggerImpl());
        this.mailHelper = new OutgoingMailHelper(backdoor);
    }

    MailTestHelper configure()
    {
        try
        {
            if (!backdoor.getTestkit().getFeatureManagerControl().isOnDemand())
            {
                mailService.configureAndStartGreenMail(JIRAServerSetup.SMTP);
                int smtpPort = mailService.getSmtpPort();
                try
                {
                    backdoor.mailServers().addSmtpServer("jiratest@atlassian.com", "[JIRATEST]", smtpPort);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Adding smtp server failed. Check that no -Datlassian.mail.senddisabled=true parameter is present.", e);
                }
            }
        }
        catch (BindException e)
        {
            throw new RuntimeException(e);
        }

        //check if mail sending is enabled (is smtp server, no disable flag)
        if (!backdoor.getTestkit().mailServers().isSmtpConfigured())
        {
            throw new RuntimeException("Smtp is not configured properly. Check if smtp server was added, and no -Datlassian.mail.senddisabled=true parameter is present.");
        }

        backdoor.outgoingMailControl().enable();
        backdoor.outgoingMailControl().clearMessages();

        return this;
    }

    public String getSentMailContent() throws IOException, MessagingException
    {
        final MimeMessage mail = mailHelper.flushMailQueueAndWait(1, 1000).iterator().next();
        MimeMultipart mailContent = (MimeMultipart) mail.getContent();
        return IOUtils.toString(mailContent.getBodyPart(0).getInputStream());
    }
}
