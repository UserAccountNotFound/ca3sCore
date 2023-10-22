package de.trustable.ca3s.core.acme;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import de.trustable.ca3s.core.PreferenceTestConfiguration;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.CaConfigTestConfiguration;
import de.trustable.ca3s.core.PipelineTestConfiguration;

// for info : test string for acme.sh:
// acme.sh --renew --force -d ubuntu.trustable.eu --standalone --alpn --tlsport 8443 --server http://192.168.56.1:9090/acme/acmeTest/directory

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ClientCertBotIT {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCertBotIT.class);


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

	@LocalServerPort
	int serverPort; // random port chosen by spring test

	final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
	String dirUrl;

	@Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @BeforeEach
	void init()  {
		dirUrl = "http://localhost:" + serverPort + ACME_PATH_PART;
		ptc.getInternalACMETestPipelineLaxRestrictions();
        prefTC.getTestUserPreference();

    }


	@Test
	public void testAccountHandling() throws IOException {

		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

		ProcessBuilder builder = new ProcessBuilder();
		if (isWindows) {
		    LOG.info("certbot test no available on Windows");
		} else {

            File webrootFolder= folder.newFolder("webroot");
            File configFolder= folder.newFolder("config");
            File workFolder= folder.newFolder("work");
            File logFolder= folder.newFolder("log");


            builder.command("certbot", "certonly",  "-n", "-v", "--debug", "--agree-tos",
		    		"--server", dirUrl,
		    		"--standalone" ,
		    		"--email", "foo@foo.de",
		    		"--preferred-challenges", "http",
		    		"-d", "ejbca.trustable.eu",
		    		"--webroot-path", webrootFolder.getAbsolutePath(),
		    		"--config-path", configFolder.getAbsolutePath(),
		    		"--work-path", workFolder.getAbsolutePath(),
		    		"--log-path", logFolder.getAbsolutePath() );

			int exitCode = executeExternalProcess(builder);

			assertEquals("expecte an exit code == 0", 0, exitCode);

		}
	}

	/**
	 * @param builder
	 */
	private int executeExternalProcess(ProcessBuilder builder) {

		int exitCode = -1;

		String cmd = "";
	    for( String s:builder.command()) {
	    	cmd += s + " ";
	    }
		LOG.debug("certbot command '"+ cmd +"' " );

		try {

//			builder.directory(new File(System.getProperty("user.home")));
			builder.inheritIO();

			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			ExecutorService execSrv = Executors.newSingleThreadExecutor();
			execSrv.submit(streamGobbler);

			exitCode = process.waitFor();
			LOG.debug("genpse exitCode '"+exitCode +"' " );

			execSrv.shutdownNow();

		}catch(InterruptedException | IOException ex) {
			LOG.error("executing external process failed with exception", ex);
		}

		return exitCode;
	}

	private static class StreamGobbler implements Runnable {
	    private InputStream inputStream;
	    private Consumer<String> consumer;

	    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
	        this.inputStream = inputStream;
	        this.consumer = consumer;
	    }

	    @Override
	    public void run() {
	        new BufferedReader(new InputStreamReader(inputStream)).lines()
	          .forEach(consumer);
	    }
	}

}
