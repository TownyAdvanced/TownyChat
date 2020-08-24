package com.palmergames.bukkit.TownyChat;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexFormatter {

    public static final Pattern hexPattern = Pattern.compile("(?<!\\\\)(#[a-fA-F0-9]{6})");
    public static final Pattern ampersandPattern = Pattern.compile("(?<!\\\\)(&#[a-fA-F0-9]{6})");
    public static final Pattern bracketPattern = Pattern.compile("(?<!\\\\)\\{(#[a-fA-F0-9]{6})}");

    public static String translateHexColors(String str) {
        final Matcher hexMatcher = hexPattern.matcher(str);
        final Matcher ampMatcher = ampersandPattern.matcher(str);
        final Matcher bracketMatcher = bracketPattern.matcher(str);

        while (hexMatcher.find()) {
            String hex = hexMatcher.group();
            str = str.replace(hex, ChatColor.of(hex).toString());
        }

        while (ampMatcher.find()) {
            String hex = ampMatcher.group().replace("&", "");
            str = str.replace(hex, ChatColor.of(hex).toString());
            str = str.replace("&", "");
        }

        while (bracketMatcher.find()) {
            String hex = bracketMatcher.group().replace("{", "").replace("}", "");
            str = str.replace(hex, ChatColor.of(hex).toString());
            str = str.replace("{", "").replace("}", "");
        }

        return str;
    }

}
