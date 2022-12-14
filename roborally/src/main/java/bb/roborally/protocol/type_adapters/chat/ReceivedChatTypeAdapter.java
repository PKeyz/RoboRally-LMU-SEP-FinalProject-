package bb.roborally.protocol.type_adapters.chat;

import bb.roborally.protocol.chat.ReceivedChat;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * @author Zeynab Baiani
 */
public class ReceivedChatTypeAdapter extends TypeAdapter<ReceivedChat> {

    @Override
    public void write(JsonWriter jsonWriter, ReceivedChat receivedChat) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("message").value(receivedChat.getMessage());
        jsonWriter.name("from").value(receivedChat.getFrom());
        jsonWriter.name("isPrivate").value(receivedChat.isPrivate());
        jsonWriter.endObject();
    }

    @Override
    public ReceivedChat read(JsonReader jsonReader) throws IOException {
        ReceivedChat receivedChat = new ReceivedChat();
        jsonReader.beginObject();
        String name;
        while (jsonReader.hasNext()){
            name = jsonReader.nextName();
            if(name.equals("message")){
                receivedChat.setMessage(jsonReader.nextString());
            }
            if(name.equals("from")){
                receivedChat.setFrom(jsonReader.nextInt());
            }
            if(name.equals("isPrivate")){
                receivedChat.setPrivate(jsonReader.nextBoolean());
            }
        }
        jsonReader.endObject();
        return receivedChat;
    }
}
