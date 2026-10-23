package net.hyze.core.shared.commands.argument.impl;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.argument.Argument;
import net.hyze.core.shared.commands.argument.ArgumentValidationResponse;
import net.hyze.core.shared.misc.utils.Patterns;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class NickArgument extends Argument {

    private boolean checkUser = false;

    public NickArgument(String name, String description, boolean required, boolean checkUser) {
        super(name, description, required);

        this.checkUser = checkUser;
    }

    public NickArgument(String name, String description, boolean required) {
        super(name, description, required);
    }

    public NickArgument(String name, String description) {
        super(name, description);
    }

    @Override
    public ArgumentValidationResponse validate(String arg) {
        if (!Patterns.NICK.matches(arg)) {
            return ArgumentValidationResponse.EMPTY_ERROR;
        }

        if (this.checkUser) {
            if (CoreProvider.Cache.Local.USERS.provide().get(arg) == null) {
                return ArgumentValidationResponse.EMPTY_ERROR;
            }
        }

        return ArgumentValidationResponse.SUCCESS;
    }

    @Override
    public BaseComponent[] getErrorMessage(String arg) {
        return TextComponent.fromLegacyText("O nick inserido é inválido.");
    }

}
