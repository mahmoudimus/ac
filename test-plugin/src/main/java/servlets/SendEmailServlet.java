package servlets;

import com.atlassian.plugin.remotable.api.service.EmailSender;
import com.atlassian.mail.Email;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class SendEmailServlet extends HttpServlet
{
    private final EmailSender emailSender;

    @Inject
    public SendEmailServlet(EmailSender emailSender)
    {
        this.emailSender = emailSender;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        Email email = new Email(req.getParameter("to"))
                .setFrom(req.getParameter("from"))
                .setSubject(req.getParameter("subject"))
                .setBody(req.getParameter("body"));
        sendEmail(email);
    }

    public void sendEmail(Email email)
    {
        emailSender.send(email.getTo(), email);
    }
}
