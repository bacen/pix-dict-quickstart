package br.gov.bcb.pi.dict;

import br.gov.bcb.pi.dict.api.model.*;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public class ObjectFactory {

    private static final NameGenerator NAME_GENERATOR = new NameGenerator();

    public static Entry createRandomEntry(String ispb) {
        Entry entry = new Entry();
        BrazilianAccount account = new BrazilianAccount()
                .participant(ispb)
                .accountNumber(randomNumeric(5))
                .branch(randomNumeric(4))
                .accountType(AccountType.CACC);

        Name randomName = NAME_GENERATOR.generateName();
        NaturalPerson owner = new NaturalPerson()
                .name(randomName.getFirstName() + " " + randomName.getLastName())
                .taxIdNumber(randomNumeric(11));

        entry.account(account)
                .key("+55119" + randomNumeric(8))
                .keyType(KeyType.PHONE)
                .owner(owner);

        entry.owner(owner);

        return entry;
    }

}
