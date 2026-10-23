package net.hyze.core.shared.commands.argument;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class ArgumentValidationResponse {

    public static final ArgumentValidationResponse SUCCESS = new ArgumentValidationResponse(true);
    public static final ArgumentValidationResponse EMPTY_ERROR = new ArgumentValidationResponse(false);

    private final boolean success;
    private BaseComponent[] errorMessage;

}