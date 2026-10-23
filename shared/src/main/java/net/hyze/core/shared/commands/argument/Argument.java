package net.hyze.core.shared.commands.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Argument {

    private final String name;
    private final String description;
    private boolean required = true;

    public String getName() {
        return (required ? "<" : "[") + name + (required ? ">" : "]");
    }

    public ArgumentValidationResponse validate(String arg) {
        return ArgumentValidationResponse.SUCCESS;
    }

    public BaseComponent[] getErrorMessage(String arg) {
        return TextComponent.fromLegacyText("");
    }

}
