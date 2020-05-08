package br.gov.bcb.pi.dict;

import br.gov.bcb.pi.dict.api.model.Entry;
import br.gov.bcb.pi.dict.api.model.LegalPerson;
import br.gov.bcb.pi.dict.api.model.NaturalPerson;
import com.google.common.hash.Hashing;

import java.nio.ByteBuffer;
import java.util.UUID;

import static com.google.common.base.Charsets.UTF_8;

public class Utils {

    private static final String SEPARATOR = "&";

    public static String cidForEntry(Entry entry, UUID requestId) {
        byte[] requestIdBytes = uuidAsBytes(requestId);

        String taxIdNumber = null;
        String name = null;
        String tradeName = null;
        if (entry.getOwner() instanceof NaturalPerson) {
            NaturalPerson owner = (NaturalPerson) entry.getOwner();
            taxIdNumber = owner.getTaxIdNumber();
            name = owner.getName();
        } else if (entry.getOwner() instanceof LegalPerson) {
            LegalPerson owner = (LegalPerson) entry.getOwner();
            taxIdNumber = owner.getTaxIdNumber();
            name = owner.getName();
            tradeName = owner.getTradeName();
        }

        return Hashing.hmacSha256(requestIdBytes).newHasher()
                .putString(entry.getKeyType().name(), UTF_8).putString(SEPARATOR, UTF_8)
                .putString(entry.getKey(), UTF_8).putString(SEPARATOR, UTF_8)
                .putString(taxIdNumber, UTF_8).putString(SEPARATOR, UTF_8)
                .putString(name, UTF_8).putString(SEPARATOR, UTF_8)
                .putString(tradeName == null ? "" : tradeName, UTF_8).putString(SEPARATOR, UTF_8)
                .putString(entry.getAccount().getParticipant(), UTF_8).putString(SEPARATOR, UTF_8)
                .putString(entry.getAccount().getBranch() == null ? "" : entry.getAccount().getBranch(), UTF_8).putString(SEPARATOR, UTF_8)
                .putString(entry.getAccount().getAccountNumber(), UTF_8).putString(SEPARATOR, UTF_8)
                .putString(entry.getAccount().getAccountType().name(), UTF_8)
                .hash().toString().toLowerCase();

    }

    public static byte[] uuidAsBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer.wrap(bytes)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return bytes;
    }
}
