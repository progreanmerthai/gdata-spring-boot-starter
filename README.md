# gdata-spring-boot-starter
gdata spring boot autoconfigurer and sarter project

## how it's working?

1. You should generate `client_secrets.json` first and then put it into classpath of Your web-application. As usual in resources folder.

2. app.properties in You web-application should have following properties:
```
cane.brothers.spring.google.app-name=
cane.brothers.spring.google.spreadsheet-name=<SPREEDSHEET-NAME>
cane.brothers.spring.google.datastore-dir=
cane.brothers.spring.google.skopes=
cane.brothers.spring.google.client-secrets=
cane.brothers.spring.google.service-url=
```

default values are :
```
cane.brothers.spring.google.app-name=app
cane.brothers.spring.google.spreadsheet-name=
cane.brothers.spring.google.datastore-dir=.store/gdata-app
cane.brothers.spring.google.skopes=https://spreadsheets.google.com/feeds
cane.brothers.spring.google.client-secrets=client_secrets.json
cane.brothers.spring.google.service-url=https://spreadsheets.google.com/feeds/spreadsheets/private/full
```

notes:
 - If no `spreadsheet-name` property defined in app.properties file then no autoconfiguration. It's mandatory field. 
 - if You need a few scopes just enumerate it using `,`
 - if You need, use another `service-url`
 
3. in console You should see something like this:
```
https://accounts.google.com/o/oauth2/auth?client_id=801002347666-cq1c446llc1k8c66o33ct3leube28ue1.apps.googleusercontent.com&redirect_uri=http://localhost:61937/Callback&response_type=code&scope=https://spreadsheets.google.com/feeds 
```
open it in brouser and accept google application engagement 
 
4. If previous steps are fine then gdata stuff will autoconfigured and started. In such case You can retreview Spreadsheet service and/or Spreadsheet entry (table) like this:
```
  @Autowired(required = false)
	private SpreadsheetService googleService;
	
	@Autowired(required = false)
	private SpreadsheetEntry googleTable;
```

5. if You need properties - do like this
```
@Controller
@EnableConfigurationProperties(GoogleProperties.class)
public class HomeController {

	@Autowired
	private GoogleProperties properties;
	
	// ...
}
```
