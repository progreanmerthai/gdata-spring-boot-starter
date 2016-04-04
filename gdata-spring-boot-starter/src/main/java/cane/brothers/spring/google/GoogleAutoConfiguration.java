package cane.brothers.spring.google;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;

@Configuration
@ConditionalOnClass({ Credential.class, SpreadsheetService.class })
@ConditionalOnProperty(prefix = "cane.brothers.spring.google", name = "spreadsheet-name")
@ConditionalOnResource(resources = { "client_secrets.json" })
@EnableConfigurationProperties(GoogleProperties.class)
public class GoogleAutoConfiguration {

	private static final Logger log = LoggerFactory.getLogger(GoogleAutoConfiguration.class);

	@Autowired
	private GoogleProperties properties;

	@Bean
	public Credential credential() {
		Credential credential = null;
		try {
			if (log.isInfoEnabled()) {
				log.info("Подключаюсь к Google сервису...");
			}
			// credential = GoogleAuthorization.authorize();
			if (log.isDebugEnabled()) {
				log.debug("Google авторизация:");
			}

			// load client secrets
			ClassPathResource resource = new ClassPathResource(properties.getClientSecrets());
			InputStreamReader clientSecretsReader = new InputStreamReader(resource.getInputStream());

			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),
					clientSecretsReader);
			if (log.isDebugEnabled()) {
				log.debug("Клиентские авторизационные данные из файла client_secrets.json подгрузили");
			}

			// if (clientSecrets.getDetails().getClientId().startsWith("Enter")
			// || clientSecrets.getDetails().getClientSecret().startsWith("Enter
			// ")) {
			// log.warn("Enter Client ID and Secret from
			// https://code.google.com/apis/console/ "
			// + "into client_secrets.json");
			//
			// return null;
			// }

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataStoreDir());

			// set up authorization code flow
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
					JacksonFactory.getDefaultInstance(), clientSecrets, skopes()).setDataStoreFactory(dataStoreFactory)
							.build();
			AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver());

			if (log.isDebugEnabled()) {
				log.debug("Авторизуемся приложением для доступа в Google API");
			}

			// authorize
			credential = app.authorize("user");

			if (credential.refreshToken()) {
				if (log.isDebugEnabled()) {
					log.debug("Token был обновлен");
				}
			}
			if (log.isInfoEnabled()) {
				log.info("Авторизация в Google есть");
			}
		}

		catch (UnknownHostException ex) {
			log.error("Нет связи с " + ex.getMessage());
			log.error("Авторизация в Google не получена");
			log.error("Проверьте соединение с интернет");
		}

		catch (Exception e) {
			log.error("Проблемы с авторизаций: ", e);
			log.error("Проверьте соединение с интернет");
		}

		// log.error("Не могу прочитать таблицу баркодов", ex);

		return credential;
	}

	/**
	 * Connect to google service with credential
	 * 
	 * @return SpreadsheetService
	 */
	@Bean
	public SpreadsheetService googleService() {
		SpreadsheetService googleService = new SpreadsheetService(properties.getAppName());
		// googleService.setProtocolVersion(SpreadsheetService.Versions.V3);
		googleService.setOAuth2Credentials(credential());

		if (log.isDebugEnabled()) {
			log.debug("К Google сервису таблиц с помощью OAuth2 подключились");
		}
		return googleService;
	}

	@Bean
	public List<String> skopes() {
		return Arrays.asList(properties.getSkopes());
	}

	@Bean
	public java.io.File dataStoreDir() {
		return new java.io.File(System.getProperty("user.home"), properties.getDatastoreDir());
	}

	@Bean
	public String spreadsheetName() {
		return properties.getSpreadsheetName();
	}

	@Bean
	public SpreadsheetEntry googleTable() {
		return googleSpreadsheetEntry(spreadsheetName(), 0);
	}

	@Bean
	URL spreadSheetFeedUrl() throws MalformedURLException {
		return new URL(properties.getServiceUrl());
	}

	/**
	 * get table.
	 * 
	 * at this moment only first Spreadsheet
	 * 
	 * @return
	 */
	@Bean
	public SpreadsheetEntry googleSpreadsheetEntry(String tableName, int page) {
		SpreadsheetEntry table = null;

		try {
			if (log.isDebugEnabled()) {
				log.debug("Подгружаемся к таблице google через запрос по ссылке");
			}
			
			SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(spreadSheetFeedUrl());
			spreadsheetQuery.setTitleQuery(spreadsheetName());
			spreadsheetQuery.setTitleExact(true);

			SpreadsheetFeed spreadsheet = googleService().getFeed(spreadsheetQuery, SpreadsheetFeed.class);

			if (spreadsheet.getEntries() != null) {
				if (log.isDebugEnabled()) {
					log.debug("Google сервис предоставил {} таблиц", spreadsheet.getEntries().size());
				}
				// TODO
				if (spreadsheet.getEntries().size() == 1) {
					return spreadsheet.getEntries().get(page);
				}
			} else {
				log.warn("Google сервис не предоставил таблиц");
				return null;
			}
		} catch (Exception ex) {
			log.error("Проблемы при запросе таблицы с google.", ex);
			return null;
		}

		if (log.isDebugEnabled()) {
			log.debug("Доступ к Google таблице {} есть", properties.getSpreadsheetName());
		}
		return table;
	}
}
