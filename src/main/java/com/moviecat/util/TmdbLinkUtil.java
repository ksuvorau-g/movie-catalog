package com.moviecat.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TmdbLinkUtil {

    private static final Pattern TMDB_TV_ID_PATTERN = Pattern.compile("/tv/(\\d+)");
    private static final Pattern TMDB_MOVIE_ID_PATTERN = Pattern.compile("/movie/(\\d+)");

    /**
     * Build TMDB link from tmdbId.
     *
     * @param tmdbId TMDB ID
     * @param isMovie true for movie, false for series
     * @return TMDB URL or null if tmdbId is null
     */
    public static String buildTmdbLink(Integer tmdbId, boolean isMovie) {
        if (tmdbId == null) {
            return null;
        }

        String type = isMovie ? "movie" : "tv";
        return "https://www.themoviedb.org/" + type + "/" + tmdbId;
    }

    /**
     * Parse TMDB ID from link if it's a TMDB URL.
     *
     * @param link URL string (may be null)
     * @return TMDB ID if found, null otherwise
     */
    public static Integer parseTmdbIdTv(String link) {
        if (link == null || link.isEmpty()) {
            return null;
        }

        Matcher matcher = TMDB_TV_ID_PATTERN.matcher(link);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ex) {
                log.warn("Failed to parse TMDB ID from link: {}", link, ex);
            }
        }

        return null;
    }

    /**
     * Parse TMDB ID from link if it's a TMDB URL.
     *
     * @param link URL string (may be null)
     * @return TMDB ID if found, null otherwise
     */
    public static Integer parseTmdbIdMovie(String link) {
        if (link == null || link.isEmpty()) {
            return null;
        }

        Matcher matcher = TMDB_MOVIE_ID_PATTERN.matcher(link);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ex) {
                log.warn("Failed to parse TMDB ID from link: {}", link, ex);
            }
        }

        return null;
    }


    /**
     * Build comment field by combining original comment with non-TMDB links.
     *
     * @param originalComment the comment from request
     * @param link the link from request
     * @param tmdbId parsed TMDB ID (null if link was not TMDB)
     * @return combined comment
     */
    public static String buildComment(String originalComment, String link, Integer tmdbId) {
        // If link is null, empty, or was a TMDB link (tmdbId extracted), just return original comment
        if (link == null || link.isEmpty() || tmdbId != null) {
            return originalComment;
        }

        // Link exists but is not a TMDB link - append to comment
        if (originalComment == null || originalComment.isEmpty()) {
            return link;
        } else {
            return originalComment + "\n" + link;
        }
    }
}
