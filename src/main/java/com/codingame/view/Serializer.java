package com.codingame.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Serializer {

	static public String serialize(FrameViewData frameViewData) {
		List<String> lines = new ArrayList<>();
		lines.add(
			join(
				frameViewData.round,
				frameViewData.gravity
			)
		);
		//ChipData
		lines.add(String.valueOf(frameViewData.chips.size()));
		frameViewData.chips.stream().forEach(chipData -> {
			lines.add(
				join(
					chipData.index,
					chipData.color,
					chipData.owner,
					chipData.q,
					chipData.r
				)
			);
		});
		frameViewData.players.stream().forEach(playerData -> {
			lines.add(
				join(
					playerData.score
				)
			);
		});
		return lines.stream().collect(Collectors.joining("\n"));
	}

	static public String serialize(GlobalViewData globalViewData) {
		List<String> lines = new ArrayList<>();
		lines.add(
			String.valueOf(globalViewData.totalRounds)
		);

		globalViewData.cells.stream().forEach(cellData -> {
			lines.add(
				join(
					cellData.q,
					cellData.r,
					cellData.index
				)
			);
		});

		return lines.stream().collect(Collectors.joining("\n"));
	}

	static public String join(Object... args) {
		return Stream.of(args)
			.map(String::valueOf)
			.collect(Collectors.joining(" "));
	}

}
