package com.hcimsafetycheck;

import java.util.Set;
import net.runelite.api.coords.WorldPoint;

public enum SafeArea {
	BARBARIAN_ASSAULT("Barbarian Assault", Set.of(7508, 7509), true),
	CAMELOT_TRAINING_ROOM("Camelot Training Room", Set.of()),   // not mapped yet
	CASTLE_WARS("Castle Wars", Set.of(9520)),
	CHAMBERS_OF_XERIC("Chambers of Xeric",
			Set.of(12889, 13145, 13401,                                  // y = 89 (Great Olm and upper rooms)
					13141, 13397, 13140, 13396, 13139, 13395,             // y = 85, 84, 83
					13138, 13394, 13137, 13393, 13136),                   // y = 82, 81, 80
			true),
	CLAN_WARS("Clan Wars",
			Set.of(12623, 12622, 12621,              // Turrets, Clan Cup Arena
					13135, 13134, 13133, 13390,       // Wasteland, Plateau, Ethereal
					13647, 13646, 13645, 13644,       // Forsaken Quarry, Sylvan Glade, Ghastly Swamp, Northleach Quell
					13131, 13130, 13387, 13386,       // Free-for-all
					13643, 13642, 13641,              // Lumbridge Castle, Classic
					13898, 14154)),                   // Falador Park (spans two regions)
	DREAM_WORLD("Dream World (Lunar Diplomacy)", Set.of()),     // not mapped yet
	EMIRS_ARENA("Emir's Arena", Set.of(13362)),
	FISHING_TRAWLER("Fishing Trawler", Set.of(7499, 7755, 8011)),
	FREMENNIK_TRIALS("Fremennik Trials (Koschei)", Set.of()),   // not mapped yet
	INFERNO("Inferno", Set.of(9043), true),
	LAST_MAN_STANDING("Last Man Standing",
			Set.of(13918, 13919, 13920, 14174, 14175, 14176, 14430, 14431, 14432,   // Wild Varrock
					13658, 13659, 13914, 13915)),                                     // Deserted Island
	MAGIC_TRAINING_ARENA("Magic Training Arena", Set.of()),     // not mapped yet
	NIGHTMARE_ZONE("Nightmare Zone", Set.of(9033), true),
	PEST_CONTROL("Pest Control", Set.of(10536)),
	PLAYER_OWNED_HOUSE("Player-owned House", Set.of(7513, 7769), true),
	PVM_ARENA("PvM Arena", Set.of(6729)),
	SOUL_WARS("Soul Wars", Set.of(7773, 8029, 8285), true),
	FIGHT_CAVE("TzHaar Fight Cave", Set.of(9551), true),
	// Bounds exclude the waiting area, which shares region 9552: {minX, maxX, minY, maxY}
	FIGHT_PIT("TzHaar Fight Pit", Set.of(9552), false, new int[]{2371, 2425, 5124, 5169}),
	ROGUES_DEN_MAZE("Rogues' Den (Maze)", Set.of(11854, 11855, 12110, 12111));

	// Zulrah intentionally omitted (requires Elite Western Diary, once per day).
	// Galvek and Glough replays omitted for now (needs a quest-state check).
	// PvM Arena may be wrong, I don't have an account with access to verify.

	private final String displayName;
	private final Set<Integer> regionIds;
	private final boolean instanceOnly;
	private final int[] bounds; // {minX, maxX, minY, maxY}, or null for region-only

	SafeArea(String displayName, Set<Integer> regionIds) {
		this(displayName, regionIds, false, null);
	}

	SafeArea(String displayName, Set<Integer> regionIds, boolean instanceOnly) {
		this(displayName, regionIds, instanceOnly, null);
	}

	SafeArea(String displayName, Set<Integer> regionIds, boolean instanceOnly, int[] bounds) {
		this.displayName = displayName;
		this.regionIds = regionIds;
		this.instanceOnly = instanceOnly;
		this.bounds = bounds;
	}

	public boolean matches(WorldPoint wp, boolean inInstance) {
		if (!regionIds.contains(wp.getRegionID()) || (instanceOnly && !inInstance)) {
			return false;
		}
		return bounds == null
				|| (wp.getX() >= bounds[0] && wp.getX() <= bounds[1]
				&& wp.getY() >= bounds[2] && wp.getY() <= bounds[3]);
	}

	@Override
	public String toString() {
		return displayName;
	}

	public static boolean isSafe(WorldPoint wp, boolean inInstance) {
		for (SafeArea area : values()) {
			if (area.matches(wp, inInstance)) {
				return true;
			}
		}
		return false;
	}
}