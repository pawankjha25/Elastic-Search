package com.searchApplication.es.filtering;

import java.io.IOException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.loader.JsonSettingsLoader;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.searchApplication.es.entities.TimeSeriesData;
import com.searchApplication.utils.IOUtils;

@ClusterScope( scope = ESIntegTestCase.Scope.SUITE, numClientNodes = 0, numDataNodes = 1 )
// TODO: something is wrong with the dw logging so it looks like it is leaking
//@ThreadLeakScope(com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope.NONE)
public abstract class SearchESTest extends ESIntegTestCase {

	static
	{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
	}

	private static final String DEFAULT_ARTICLE_MAPPING = "src/test/resources/index/updatedRow.json";
	protected static final String TYPE_NAME = "time_series";
	public static final String TEST_INDEX_NAME = "time_series";
	protected static final String TEST_ANALYZER = "standard";

	private final ObjectMapper MAPPER = new ObjectMapper();
	JsonSettingsLoader settingsParser = new JsonSettingsLoader();

	@Override
	public void allowNodes( String index, int n )
	{
		super.allowNodes(index, 0);
	}

	public void createIndex( String name, String type, String mappingFile ) throws IOException
	{
		JsonSettingsLoader js = new JsonSettingsLoader();
		String analyzerSettings = IOUtils.textLines("src/test/resources/index/analyzer.json");
		Settings set = Settings.builder().put(js.load(analyzerSettings)).build();

		client().admin().indices().prepareCreate(TEST_INDEX_NAME).setSettings(set).get();
		String mapping = IOUtils.textLines(mappingFile);
		client().admin().indices().preparePutMapping(TEST_INDEX_NAME).setSource(mapping).setType(type).get();
	}

	public void createTestIndex() throws IOException
	{
		createIndex(TEST_INDEX_NAME, TYPE_NAME, DEFAULT_ARTICLE_MAPPING);
	}

	@SuppressWarnings( "deprecation" )
	public void index( TimeSeriesData r, String id ) throws IOException
	{
		MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
		MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		MAPPER.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
		MAPPER.setSerializationInclusion(Include.NON_NULL);
		client().prepareIndex().setIndex(TEST_INDEX_NAME).setType(TYPE_NAME).setId(id)
				.setSource(MAPPER.writeValueAsBytes(r)).get();
		refresh();
	}

	@Override
	protected Settings nodeSettings( int nodeOrdinal )
	{
		try
		{

			String analyzerSettings = IOUtils.textLines("src/test/resources/index/analyzer.json");
			Settings set = Settings.builder().put(settingsParser.load(analyzerSettings))
					.put(super.nodeSettings(nodeOrdinal)).build();

			return set;
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return super.nodeSettings(1);

	}

}
