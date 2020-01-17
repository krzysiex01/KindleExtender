package kindleExtender.helpers;

import com.squareup.okhttp.*;
import kindleExtender.models.Word;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class TranslateHelper {
    private static String subscriptionKey = System.getenv("TRANSLATOR_TEXT_SUBSCRIPTION_KEY");
    private static String endpoint = System.getenv("TRANSLATOR_TEXT_ENDPOINT");

    public void translate(Word word, String to) throws IOException {
        String url = endpoint + "&from=" + word.getLanguage() + "&to=" + to;
        String response = null;

        response = post(url, word.getStem());
        parseResponse(response, word);
        word.setTranslationTo(to);
    }

    public void translate(List<Word> words, String to) throws IOException {
        for (var word:words) {
            if (Objects.equals(word.getLanguage(), to))
                continue;
            translate(word, to);
        }
    }

    private String post(String url, String value) throws IOException {
        // Create http client
        OkHttpClient client = new OkHttpClient();

        // Construct request
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "[{\n\t\"Text\": \"" + value + "\"\n}]");
        Request request = new Request.Builder()
                .url(url).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .addHeader("Content-type", "application/json").build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void parseResponse(String json_text, Word word) {
        String translateValue = "";
        JSONArray responseArray = new JSONArray(json_text);
        JSONObject responseObject = responseArray.getJSONObject(0);
        JSONArray translations = responseObject.getJSONArray("translations");

        if (translations.length() == 0) {
            word.setTranslationValue("-");
            return;
        }

        for (int i = 0; i < translations.length() && i < 4; i++) {
            double confidence = translations.getJSONObject(i).getDouble("confidence");

            translateValue += translations.getJSONObject(i).getString("displayTarget").toLowerCase() + ", ";
        }
        translateValue = translateValue.substring(0,translateValue.length() - 2);
        word.setPartOfSpeech(translations.getJSONObject(0).getString("posTag"));
        word.setTranslationValue(translateValue);
    }
}
