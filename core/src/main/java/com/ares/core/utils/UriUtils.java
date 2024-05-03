package com.ares.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wesley
 */

@Slf4j
public abstract class UriUtils {

    public static List<URI> stringListAsUriList(final List<String> stringList) {
		List<URI> uriList = new ArrayList<>();
        try {
            for (String url : stringList) {
                uriList.add(URI.create(url));
            }
        } catch (Exception e) {
            log.error("Exception in URI.create", e);
        }
		return uriList;
    }
}
