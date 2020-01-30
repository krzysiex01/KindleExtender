package kindleExtender.helpers;

import com.squareup.okhttp.*;
import kindleExtender.TranslateCallbacks;
import kindleExtender.models.Word;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class TranslateHelper {
    private static String subscriptionKey = System.getenv("TRANSLATOR_TEXT_SUBSCRIPTION_KEY");
    private static String endpoint = System.getenv("TRANSLATOR_TEXT_ENDPOINT");

    public void translate(Word word, String to) {
        String url = endpoint + "&from=" + word.getLanguage() + "&to=" + to;

        post(url, word.getStem(), new TranslateCallbacks() {
            @Override
            public void onSuccess(String value) {
                parseResponse(value, word);
                word.setTranslationTo(to);
            }

            @Override
            public void onError(Throwable throwable) {
                //throwable.printStackTrace();
            }
        });
    }

    public void translate(List<Word> words, String to) throws IOException {
        for (var word:words) {
            if (Objects.equals(word.getLanguage(), to)) {
                continue;
            }
        }
    }

    private void post(String url, String value, TranslateCallbacks callbacks) {
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

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (callbacks != null)
                    callbacks.onError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                    if (callbacks != null)
                        callbacks.onSuccess(response.body().string());
                }
            }
        });
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
