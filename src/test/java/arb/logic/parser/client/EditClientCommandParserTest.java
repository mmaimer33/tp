package arb.logic.parser.client;

import static arb.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static arb.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static arb.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static arb.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static arb.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static arb.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static arb.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static arb.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static arb.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static arb.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static arb.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static arb.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static arb.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static arb.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static arb.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static arb.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static arb.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static arb.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static arb.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static arb.logic.parser.CliSyntax.PREFIX_TAG;
import static arb.logic.parser.CommandParserTestUtil.assertParseFailure;
import static arb.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static arb.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static arb.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;
import static arb.testutil.TypicalIndexes.INDEX_THIRD_CLIENT;

import org.junit.jupiter.api.Test;

import arb.commons.core.index.Index;
import arb.logic.commands.client.EditClientCommand;
import arb.logic.commands.client.EditClientCommand.EditClientDescriptor;
import arb.model.client.Email;
import arb.model.client.Name;
import arb.model.client.Phone;
import arb.model.tag.Tag;
import arb.testutil.EditClientDescriptorBuilder;

public class EditClientCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditClientCommand.MESSAGE_USAGE);

    private EditClientCommandParser parser = new EditClientCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditClientCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS); // invalid tag

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // valid phone followed by invalid phone. The test case for invalid phone followed by valid phone
        // is tested at {@code parse_invalidValueFollowedByValidValue_success()}
        assertParseFailure(parser, "1" + PHONE_DESC_BOB + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Client} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_EMPTY, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_EMPTY + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_EMPTY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_CLIENT;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + TAG_DESC_HUSBAND
                + EMAIL_DESC_AMY + NAME_DESC_AMY + TAG_DESC_FRIEND;

        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY)
                .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        EditClientCommand expectedCommand = new EditClientCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_CLIENT;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditClientCommand expectedCommand = new EditClientCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_CLIENT;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditClientCommand expectedCommand = new EditClientCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditClientDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditClientCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditClientDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditClientCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // tags
        userInput = targetIndex.getOneBased() + TAG_DESC_FRIEND;
        descriptor = new EditClientDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();
        expectedCommand = new EditClientCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_acceptsLast() {
        Index targetIndex = INDEX_FIRST_CLIENT;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_BOB).withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .build();
        EditClientCommand expectedCommand = new EditClientCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_invalidValueFollowedByValidValue_success() {
        // no other valid values specified
        Index targetIndex = INDEX_FIRST_CLIENT;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        EditClientCommand expectedCommand = new EditClientCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // other valid values specified
        userInput = targetIndex.getOneBased() + EMAIL_DESC_BOB + INVALID_PHONE_DESC
                + PHONE_DESC_BOB;
        descriptor = new EditClientDescriptorBuilder().withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .build();
        expectedCommand = new EditClientCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_resetTags_success() {
        Index targetIndex = INDEX_THIRD_CLIENT;
        String userInput = targetIndex.getOneBased() + TAG_EMPTY;

        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withTags().build();
        EditClientCommand expectedCommand = new EditClientCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }
}