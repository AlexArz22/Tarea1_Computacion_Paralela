package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil 
{
    public static String fetchHttpResponse(URL url) throws IOException 
    {
    	// Se establece la conexión con la solicitud Get
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) 
        {
            throw new RuntimeException("Error: Código de error HTTP: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        // Si la respuesta es valida se lee la respuesta
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        
        connection.disconnect();
        
        return response.toString();
    }
}
