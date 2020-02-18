package br.gov.bcb.pi.dict;

import br.gov.bcb.pi.dict.api.DirectoryApi;
import br.gov.bcb.pi.dict.api.model.*;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.google.common.hash.Hashing.hmacSha256;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;


public class App {
    private static final String SECRET_SIGNING_KEY = "chave-secreta-participante";

    @Parameter(names = "-ispb", description = "ISPB do participante")
    private String ispb = "12345678";
    @Parameter(names = "-baseAddress", description = "Endereço-base da API")
    private String baseAddress = "https://dict-h.pi.rsfn.net.br/dict/api/v1";

    public static void main(String[] args) throws IOException {
        App main = new App();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);
        main.run();
    }

    public void run() {
        DirectoryApi dictApi = ApiClientFactory.createApiClient(baseAddress);

        // cria um vínculo no DICT
        Entry entry = ObjectFactory.createRandomEntry(ispb);
        CreateEntryRequest createRequest = new CreateEntryRequest().entry(entry);
        handleCreateEntryResponse(dictApi.createEntry(createRequest));

        // consulta um vínculo
        String randomCpf = randomNumeric(11);
        String piPayerId = BaseEncoding.base16()
                .encode(hmacSha256(SECRET_SIGNING_KEY.getBytes()).hashString(randomCpf, StandardCharsets.UTF_8)
                        .asBytes());
        String piEndToEndId = randomAlphanumeric(32);

        String key = "00038166000105";
        handleGetEntryResponse(dictApi.getEntry(key, ispb, piPayerId, piEndToEndId));
    }

    public void handleCreateEntryResponse(Response response) {
        Preconditions.checkNotNull(response);
        switch (response.getStatus()) {
            case 201:
                System.out.println(">> Retorno: " + response.readEntity(CreateEntryResponse.class).getEntry());
                break;
            case 400:
            case 403:
            case 503:
                System.out.println(">> Retorno: " + response.readEntity(Problem.class));
                break;
            default:
                System.out.println(">> Retorno: " + response.getStatus());
        }

    }

    public void handleGetEntryResponse(Response response) {
        Preconditions.checkNotNull(response);
        switch (response.getStatus()) {
            case 200:
                System.out.println(">> Retorno: " + response.readEntity(GetEntryResponse.class).getEntry());
                break;
            case 400:
            case 404:
                System.out.println(">> Retorno: " + response.readEntity(Problem.class));
                break;
            default:
                System.out.println(">> Retorno: " + response.getStatus());
        }

    }
}
