package cane.brothers.spring.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cane.brothers.spring.google")
public class GoogleProperties {

	private String appName = "app";
	
	private String spreadsheetName;
	
	private String skopes = "https://spreadsheets.google.com/feeds";
	
	private String datastoreDir = ".store/gdata-app";
	
	private String clientSecrets = "client_secrets.json";
	
	private String serviceUrl = "https://spreadsheets.google.com/feeds/spreadsheets/private/full";

	public String getSkopes() {
		return skopes;
	}
	
	public String getAppName() {
		return appName;
	}

	public String getSpreadsheetName() {
		return spreadsheetName;
	}

	public String getDatastoreDir() {
		return datastoreDir;
	}

	public String getClientSecrets() {
		return clientSecrets;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setSkopes(String skopes) {
		this.skopes = skopes;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setSpreadsheetName(String spreadsheetName) {
		this.spreadsheetName = spreadsheetName;
	}

	public void setDatastoreDir(String datastoreDir) {
		this.datastoreDir = datastoreDir;
	}

	public void setClientSecrets(String clientSecrets) {
		this.clientSecrets = clientSecrets;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
}
