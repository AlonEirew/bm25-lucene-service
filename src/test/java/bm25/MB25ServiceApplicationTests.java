package bm25;

import bm25.data.*;
import bm25.utils.Utils;
import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MB25ServiceApplicationTests {

	private static final Gson GSON = Utils.getGSONPrettyPrint();

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createSearchAndDeleteIndex() throws Exception {
		CreateIndexRequest request = new CreateIndexRequest(
				"src/test/resources/passages.tsv",
				"tempIndex/test");

		MvcResult mvcCreateResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/createLuceneIndex")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(GSON.toJson(request)))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.added", Matchers.is(5))).andReturn();

		CreateIndexResponse createIndexResponse = Utils.getGSONPrettyPrint().fromJson(
				mvcCreateResult.getResponse().getContentAsString(Charset.defaultCharset()), CreateIndexResponse.class);

		BM25SearchRequest searchRequest = new BM25SearchRequest(createIndexResponse.getIndexId(), "1",
				"Hajuron Jamiri", 5);
		List<String> acceptedRrankResult = new ArrayList<>();
		acceptedRrankResult.add("122791");
		this.mockMvc.perform(MockMvcRequestBuilders.post("/bm25Search")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(GSON.toJson(searchRequest)))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().json(GSON.toJson(new BM25SearchResponse("1", acceptedRrankResult,
						"Done successfully"))));

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/deleteLuceneIndex")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(createIndexResponse.getIndexId()))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().json(GSON.toJson(new DeleteIndexResponse("true", "Index deleted successfully"))));
	}
}
