package router.usage.statistics.java.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.usage.statistics.java.servlet.Servlet;

import static router.usage.statistics.java.util.Util.*;

public class ServerJetty {

    private static final Logger log = LoggerFactory.getLogger(ServerJetty.class);

    public void start() throws Exception {
        log.info("Start Jetty Server");

        QueuedThreadPool threadPool = new QueuedThreadPool(SERVER_MAX_THREADS, SERVER_MIN_THREADS, SERVER_IDLE_TIMEOUT);
        Server server = new Server(threadPool);

        try (ServerConnector connector = new ServerConnector(server)) {
            int port = getSystemEnvProperty(SERVER_PORT) == null ? 8000 : Integer.parseInt(getSystemEnvProperty(SERVER_PORT));
            connector.setPort(port);
            server.setConnectors(new Connector[]{connector});
        }

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(Servlet.class, "/");

        server.setHandler(servletHandler);
        server.start();
        log.info("Finish Jetty Server");
    }
}
