package bb.roborally.data.messages.type_adapters;

import bb.roborally.data.messages.connection.Welcome;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class WelcomeTypeAdapter extends TypeAdapter<Welcome> {
    @Override
    public void write(JsonWriter jsonWriter, Welcome welcome) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("clientID").value(welcome.getClientID());
        jsonWriter.endObject();
    }

    @Override
    public Welcome read(JsonReader jsonReader) throws IOException {
        Welcome welcome = new Welcome();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            if (jsonReader.nextName().equals("clientID")) {
                welcome.setClientID(jsonReader.nextInt());
            }
        }
        jsonReader.endObject();
        return welcome;
    }
}
