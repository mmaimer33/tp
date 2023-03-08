package arb.testutil;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import arb.model.client.Client;
import arb.model.client.Email;
import arb.model.client.Name;
import arb.model.client.Phone;
import arb.model.tag.Tag;
import arb.model.util.SampleDataUtil;

/**
 * A utility class to help with building Client objects.
 */
public class ClientBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";

    private Name name;
    private Optional<Phone> phone;
    private Optional<Email> email;
    private Set<Tag> tags;

    /**
     * Creates a {@code ClientBuilder} with the default details.
     */
    public ClientBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = Optional.of(new Phone(DEFAULT_PHONE));
        email = Optional.of(new Email(DEFAULT_EMAIL));
        tags = new HashSet<>();
    }

    /**
     * Initializes the ClientBuilder with the data of {@code clientToCopy}.
     */
    public ClientBuilder(Client clientToCopy) {
        name = clientToCopy.getName();
        phone = Optional.ofNullable(clientToCopy.getPhone());
        email = Optional.ofNullable(clientToCopy.getEmail());
        tags = new HashSet<>(clientToCopy.getTags());
    }

    /**
     * Sets the {@code Name} of the {@code Client} that we are building.
     */
    public ClientBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and set it to the {@code Client} that we are building.
     */
    public ClientBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Client} that we are building.
     */
    public ClientBuilder withPhone(String phone) {
        this.phone = Optional.ofNullable(phone).map(p -> new Phone(p));
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Client} that we are building.
     */
    public ClientBuilder withEmail(String email) {
        this.email = Optional.ofNullable(email).map(e -> new Email(e));
        return this;
    }

    public Client build() {
        return new Client(name, phone.orElse(null), email.orElse(null), tags);
    }

}
