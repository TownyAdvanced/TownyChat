package com.palmergames.bukkit.TownyChat;

import com.palmergames.bukkit.TownyChat.config.ChatSettings;
import com.palmergames.bukkit.TownyChat.listener.LocalTownyChatEvent;
import com.palmergames.bukkit.TownyChat.util.StringReplaceManager;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.TownyUniverse;

import java.util.regex.Pattern;

public class TownyChatFormatter {
	private static StringReplaceManager<LocalTownyChatEvent> replacer = new StringReplaceManager<LocalTownyChatEvent>();

	static {
		replacer.registerFormatReplacement(Pattern.quote("{worldname}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return String.format(ChatSettings.getWorldTag(), event.getEvent().getPlayer().getWorld().getName());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{town}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return event.getResident().hasTown() ? event.getResident().getTown().getName() : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townformatted}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatTownTag(event.getResident(), false, true);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{towntag}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatTownTag(event.getResident(), false, false);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{towntagoverride}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatTownTag(event.getResident(), true, false);
			}
		});
		
		replacer.registerFormatReplacement(Pattern.quote("{nation}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return event.getResident().hasNation() ? event.getResident().getTown().getNation().getName() : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{nationformatted}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatNationTag(event.getResident(), false, true);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{nationtag}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatNationTag(event.getResident(), false, false);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{nationtagoverride}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatNationTag(event.getResident(), true, false);
			}
		});
		
		replacer.registerFormatReplacement(Pattern.quote("{townytag}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatTownyTag(event.getResident(), false, false);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townyformatted}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatTownyTag(event.getResident(), false, true);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townytagoverride}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatTownyTag(event.getResident(), true, false);
			}
		});
		
		replacer.registerFormatReplacement(Pattern.quote("{title}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return event.getResident().hasTitle() ? event.getResident().getTitle() + " " : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{surname}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return event.getResident().hasSurname() ? " " + event.getResident().getSurname() : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townynameprefix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return getNamePrefix(event.getResident());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townynamepostfix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return getNamePostfix(event.getResident());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townyprefix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return event.getResident().hasTitle() ? event.getResident().getTitle() + " " : getNamePrefix(event.getResident());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townypostfix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return event.getResident().hasSurname() ? " " + event.getResident().getSurname() : getNamePostfix(event.getResident());
			}
		});
		
		replacer.registerFormatReplacement(Pattern.quote("{townycolor}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				if (!event.getResident().hasTown())
					return ChatSettings.getNomadColour();
				else 
					return event.getResident().isMayor() ? (event.getResident().isKing() ? ChatSettings.getKingColour() : ChatSettings.getMayorColour()) : ChatSettings.getResidentColour();
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{group}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return TownyUniverse.getInstance().getPermissionSource().getPlayerGroup(event.getEvent().getPlayer());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{permprefix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return TownyUniverse.getInstance().getPermissionSource().getPrefixSuffix(event.getResident(), "prefix");
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{permsuffix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return TownyUniverse.getInstance().getPermissionSource().getPrefixSuffix(event.getResident(), "suffix");
			}
		});

		replacer.registerFormatReplacement(Pattern.quote("{permuserprefix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return TownyUniverse.getInstance().getPermissionSource().getPrefixSuffix(event.getResident(), "userprefix");
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{permusersuffix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return TownyUniverse.getInstance().getPermissionSource().getPrefixSuffix(event.getResident(), "usersuffix");
			}
		});

		replacer.registerFormatReplacement(Pattern.quote("{permgroupprefix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return TownyUniverse.getInstance().getPermissionSource().getPrefixSuffix(event.getResident(), "groupprefix");
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{permgroupsuffix}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return TownyUniverse.getInstance().getPermissionSource().getPrefixSuffix(event.getResident(), "groupsuffix");
			}
		});

		
		replacer.registerFormatReplacement(Pattern.quote("{playername}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return event.getEvent().getPlayer().getName();
			}
		});
		/*
		replacer.registerFormatReplacement(Pattern.quote("{modplayername}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, TownyChatEvent event) throws Exception {
				return event.getEvent().getPlayer().getDisplayName();
			}
		});
		*/
		replacer.registerFormatReplacement(Pattern.quote("{channelTag}"), new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return formatTownyTag(event.getResident(), false, false);
			}
		});

		// Replace colours last ({msg} is replaced last as it can't be regex parsed).
		replacer.registerFormatReplacement("&{1}[0-9A-Fa-fLlOoNnKkMmRr]{1}", new TownyChatReplacerCallable() {
			@Override
			public String call(String match, LocalTownyChatEvent event) throws Exception {
				return "\u00A7" + match.charAt(1);
			}
		});

	}

	public static String hexIfCompatible(String str) {
		if (Towny.is116Plus()) {
			return HexFormatter.translateHexColors(str);
		}

		return str;
	}

	public static String getChatFormat(LocalTownyChatEvent event) {
		// Replace the {msg} here so it's not regex parsed.
		return hexIfCompatible(replacer.replaceAll(event.getFormat(), event).replace("{modplayername}", "%1$s").replace("{msg}", "%2$s"));
	}

	/**
	 * @param resident
	 * @param override	use full names if no tag is present
	 * @param full		Only use full names (no tags).
	 * @return string containing the correctly formatted nation/town data
	 */
	public static String formatTownyTag(Resident resident, Boolean override, Boolean full) {
		try {
			if (resident.hasTown()) {
				Town town = resident.getTown();
				String townTag = town.getTag();
				Nation nation = null;
				String nationTag = null;
				if (resident.hasNation()) {
					nation = town.getNation();
					nationTag = nation.getTag();
				}

				String nTag = "", tTag = "";

				//Force use of full names only
				if (full) {
					nationTag = "";
					townTag = "";
				}
				// Load town tags/names
				if (townTag != null && !townTag.isEmpty())
					tTag = townTag;
				else if (override || full)
					tTag = town.getName();

				// Load the nation tags/names
				if ((nationTag != null) && !nationTag.isEmpty())
					nTag = nationTag;
				else if (resident.hasNation() && (override || full))
					nTag = nation.getName();

				// Output depending on what tags are present
				if ((!tTag.isEmpty()) && (!nTag.isEmpty())) {
					if (ChatSettings.getBothTags().contains("%t") || ChatSettings.getBothTags().contains("%n")) {
						// Then it contains %s & %s
						// Small suttle change so that an issue is solved, it is documented in the config.
						// But only after addition of this. (v0.50)
						return ChatSettings.getBothTags().replace("%t", tTag).replace("%n", nTag);
					} else {
						return String.format(ChatSettings.getBothTags(), nTag, tTag);
					}
				}

				if (!nTag.isEmpty()) {
					return String.format(ChatSettings.getNationTag(), nTag);
				}

				if (!tTag.isEmpty()) {
					return String.format(ChatSettings.getTownTag(), tTag);
				}

			}
		} catch (NotRegisteredException e) {
			// no town or nation
		}
		return "";
	}

	public static String formatTownTag(Resident resident, Boolean override, Boolean full) {
		try {
			if (resident.hasTown())
				if (full)
					return hexIfCompatible(String.format(ChatSettings.getTownTag(), resident.getTown().getName()));
				else if (resident.getTown().hasTag())
					return hexIfCompatible(String.format(ChatSettings.getTownTag(), resident.getTown().getTag()));
				else if (override)
					return hexIfCompatible(String.format(ChatSettings.getTownTag(), resident.getTown().getName()));

		} catch (NotRegisteredException e) {
		}
		return "";
	}

	public static String formatNationTag(Resident resident, Boolean override, Boolean full) {
		try {
			if (resident.hasNation())
				if (full)
					return hexIfCompatible(String.format(ChatSettings.getNationTag(), resident.getTown().getNation().getName()));
				else if (resident.getTown().getNation().hasTag())
					return hexIfCompatible(String.format(ChatSettings.getNationTag(), resident.getTown().getNation().getTag()));
				else if (override)
					return hexIfCompatible(String.format(ChatSettings.getNationTag(), resident.getTown().getNation().getName()));

		} catch (NotRegisteredException e) {
		}
		return "";
	}

	public static String getNamePrefix(Resident resident) {

		if (resident == null)
			return "";
		if (resident.isKing())
			return TownySettings.getKingPrefix(resident);
		else if (resident.isMayor())
			return TownySettings.getMayorPrefix(resident);
		return "";
	}

	public static String getNamePostfix(Resident resident) {

		if (resident == null)
			return "";
		if (resident.isKing())
			return TownySettings.getKingPostfix(resident);
		else if (resident.isMayor())
			return TownySettings.getMayorPostfix(resident);
		return "";
	}
}
