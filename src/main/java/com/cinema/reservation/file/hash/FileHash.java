package com.cinema.reservation.file.hash;

import com.cinema.reservation.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
