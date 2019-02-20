# demo
initializr-demo

## run: 
`mvn:spring-boot:run` 

## apply module:
`http://localhost:8080/module/apply` with request body:
```json
{
	"name":"test module",
	"groupId":"com.landray",
	"artifactId":"module-demo",
	"version":"0.0.1-SNAPSHOT",
	"description":"test teset"
}
```
then return:
```json
{
    "id": "2a9d5bdb-9488-46cc-9ab2-cbdd7c036a60",
    "name": "test module",
    "groupId": "com.landray",
    "artifactId": "module-demo",
    "version": "0.0.1-SNAPSHOT",
    "description": "test teset"
}
```
## generate app with the applied module as one dependency:
`http://localhost:8080/app/generate` with request body:
```json
{
	"name":"test app",
	"type":"maven-project",
	"description":"sdhafhhasdf",
	"groupId":"com.landray",
	"artifactId":"app-demo",
	"version":"0.0.1-SNAPSHOT",
	"bootVersion":"2.1.2.RELEASE",
	"packaging":"jar",
	"language":"java",
	"packageName":"com.lanaray.app.demo",
	"javaVersion":"1.8",
	"dependencies":["2a9d5bdb-9488-46cc-9ab2-cbdd7c036a60"]
}
```
the request body's dependencies is the applied module's id: 2a9d5bdb-9488-46cc-9ab2-cbdd7c036a60
and then return the exception:
```json
{
    "timestamp": "2019-02-20T02:30:04.686+0000",
    "status": 500,
    "error": "Internal Server Error",
    "message": "Unknown dependency '709eb0bd-b283-4f30-a736-621be8055eaa' check project metadata",
    "path": "/app/generate"
}
```
I try to update the current initializrMetadata's dependencies :
```java
public class DefaultInitializrMetadataUpdateStrategy implements InitializrMetadataUpdateStrategy {

	private static final Log logger = LogFactory
			.getLog(DefaultInitializrMetadataUpdateStrategy.class);

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;
	
	private final ModuleService moduleService;

	public DefaultInitializrMetadataUpdateStrategy(RestTemplate restTemplate,
			ObjectMapper objectMapper,ModuleService moduleService) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
		this.moduleService = moduleService;
	}

	@Override
	public InitializrMetadata update(InitializrMetadata current) {
		String url = current.getConfiguration().getEnv().getSpringBootMetadataUrl();
		List<DefaultMetadataElement> bootVersions = fetchSpringBootVersions(url);
		if (bootVersions != null && !bootVersions.isEmpty()) {
			if (bootVersions.stream().noneMatch(DefaultMetadataElement::isDefault)) {
				// No default specified
				bootVersions.get(0).setDefault(true);
			}
			current.updateSpringBootVersions(bootVersions);
		}
		List<DependencyGroup> groups = convertLocalModuleToGroup();
		current.getDependencies().merge(groups);
		logger.debug("update the initializrMetadata...");
		return current;
	}

	/**
	 * Fetch the available Spring Boot versions using the specified service url.
	 * @param url the url to the spring-boot project metadata
	 * @return the spring boot versions metadata.
	 */
	protected List<DefaultMetadataElement> fetchSpringBootVersions(String url) {
		if (StringUtils.hasText(url)) {
			try {
				logger.info("Fetching Spring Boot metadata from " + url);
				return new SpringBootMetadataReader(this.objectMapper, this.restTemplate,
						url).getBootVersions();
			}
			catch (Exception ex) {
				logger.warn("Failed to fetch Spring Boot metadata", ex);
			}
		}
		return null;
	}
	
	/**
	 * load all module and convert to dependencyGroup
	 * @return
	 */
	protected List<DependencyGroup> convertLocalModuleToGroup(){
		List<Module> list = moduleService.list();
		List<DependencyGroup> groups = new ArrayList<>();
		Map<String,List<Module>> map = list.stream().collect(Collectors.groupingBy(Module::getArtifactId));
		map.forEach((artifactid,modules)->{
			DependencyGroup group = new DependencyGroup();
			group.setName(artifactid);
			modules.forEach(module->{
				Dependency dependency = new Dependency();
				dependency.setId(module.getId());
				dependency.setGroupId(module.getGroupId());
				dependency.setArtifactId(module.getArtifactId());
				dependency.setVersion(module.getVersion());
				group.getContent().add(dependency);
			});
			groups.add(group);
		});
		return groups;
	}

}
```
But it is not work for me...
I add some code into DefaultInitializrMetadatabuilder to add all applied module to initializrMetadata's dependencies,but this is only usefull when application restart
```java
/**
	 * add all applied module to initializrMetadata's dependencies
	 * @return DefaultInitializrMetadatabuilder
	 */
	public DefaultInitializrMetadatabuilder addLocalModules(ModuleService moduleService) {
		moduleService.list().forEach(module->{
			Dependency dependency = new Dependency();
			dependency.setId(module.getId());
			dependency.setGroupId(module.getGroupId());
			dependency.setArtifactId(module.getArtifactId());
			dependency.setDescription(module.getDescription());
			dependency.setVersion(module.getVersion());
			addDependencyGroup(module.getArtifactId(),dependency);
			if(log.isDebugEnabled()) {
				log.debug("add module[{}] to dependencies",dependency);
			}
		});
		addDependencyGroup("CORE",Dependency.withId("web","org.springframework.boot", "spring-boot-starter-web"));
		return this;
	}
  ```
 I hoep that after applied one module to database an dependency it without restart the application, or can update initializrMetadata's dependencies dynamically, how to do that?
 I need yours help, Thanks.
