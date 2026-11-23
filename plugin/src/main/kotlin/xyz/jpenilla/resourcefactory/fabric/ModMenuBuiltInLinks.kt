/*
 * Resource Factory Gradle Plugin
 * Copyright (c) 2024 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jpenilla.resourcefactory.fabric

import org.gradle.api.provider.MapProperty

/**
 * Helpers for the link keys Mod Menu provides translations for.
 *
 * Mod Menu uses a link's key as the translation key for its label, so other keys are valid too
 * when the mod provides the translation itself. Without a translation, the raw key is shown.
 *
 * There are no helpers for `modmenu.website` and `modmenu.issues`: Mod Menu already shows Website and
 * Issues buttons from the `fabric.mod.json` contact information, so set those with [FabricModJson.contact] instead.
 *
 * @see ModMenu.links
 * @see ModMenu.builtInLinks
 */
class ModMenuBuiltInLinks(private val links: MapProperty<String, String>) {
    /** Adds a link with the [BLUESKY] key ("Bluesky"). */
    fun bluesky(url: String) = links.put(BLUESKY, url)

    /** Adds a link with the [BUY_ME_A_COFFEE] key ("Buy Me a Coffee"). */
    fun buyMeACoffee(url: String) = links.put(BUY_ME_A_COFFEE, url)

    /** Adds a link with the [COINDROP] key ("Coindrop"). */
    fun coindrop(url: String) = links.put(COINDROP, url)

    /** Adds a link with the [CROWDIN] key ("Crowdin"). */
    fun crowdin(url: String) = links.put(CROWDIN, url)

    /** Adds a link with the [CURSEFORGE] key ("CurseForge"). */
    fun curseforge(url: String) = links.put(CURSEFORGE, url)

    /** Adds a link with the [DISCORD] key ("Discord"). */
    fun discord(url: String) = links.put(DISCORD, url)

    /** Adds a link with the [DONATE] key ("Donate"). */
    fun donate(url: String) = links.put(DONATE, url)

    /** Adds a link with the [FLATTR] key ("Flattr"). */
    fun flattr(url: String) = links.put(FLATTR, url)

    /** Adds a link with the [GITHUB_RELEASES] key ("GitHub Releases"). */
    fun githubReleases(url: String) = links.put(GITHUB_RELEASES, url)

    /** Adds a link with the [GITHUB_SPONSORS] key ("GitHub Sponsors"). */
    fun githubSponsors(url: String) = links.put(GITHUB_SPONSORS, url)

    /** Adds a link with the [KOFI] key ("Ko-fi"). */
    fun kofi(url: String) = links.put(KOFI, url)

    /** Adds a link with the [LIBERAPAY] key ("Liberapay"). */
    fun liberapay(url: String) = links.put(LIBERAPAY, url)

    /** Adds a link with the [MASTODON] key ("Mastodon"). */
    fun mastodon(url: String) = links.put(MASTODON, url)

    /** Adds a link with the [MODRINTH] key ("Modrinth"). */
    fun modrinth(url: String) = links.put(MODRINTH, url)

    /** Adds a link with the [OPEN_COLLECTIVE] key ("Open Collective"). */
    fun openCollective(url: String) = links.put(OPEN_COLLECTIVE, url)

    /** Adds a link with the [PATREON] key ("Patreon"). */
    fun patreon(url: String) = links.put(PATREON, url)

    /** Adds a link with the [PAYPAL] key ("PayPal"). */
    fun paypal(url: String) = links.put(PAYPAL, url)

    /** Adds a link with the [REDDIT] key ("Reddit"). */
    fun reddit(url: String) = links.put(REDDIT, url)

    /** Adds a link with the [TWITCH] key ("Twitch"). */
    fun twitch(url: String) = links.put(TWITCH, url)

    /** Adds a link with the [TWITTER] key ("X (Twitter)"). */
    fun twitter(url: String) = links.put(TWITTER, url)

    /** Adds a link with the [WIKI] key ("Wiki"). */
    fun wiki(url: String) = links.put(WIKI, url)

    /** Adds a link with the [YOUTUBE] key ("YouTube"). */
    fun youtube(url: String) = links.put(YOUTUBE, url)

    companion object {
        /** `modmenu.bluesky`, shown as "Bluesky". */
        const val BLUESKY: String = "modmenu.bluesky"

        /** `modmenu.buymeacoffee`, shown as "Buy Me a Coffee". */
        const val BUY_ME_A_COFFEE: String = "modmenu.buymeacoffee"

        /** `modmenu.coindrop`, shown as "Coindrop". */
        const val COINDROP: String = "modmenu.coindrop"

        /** `modmenu.crowdin`, shown as "Crowdin". */
        const val CROWDIN: String = "modmenu.crowdin"

        /** `modmenu.curseforge`, shown as "CurseForge". */
        const val CURSEFORGE: String = "modmenu.curseforge"

        /** `modmenu.discord`, shown as "Discord". */
        const val DISCORD: String = "modmenu.discord"

        /** `modmenu.donate`, shown as "Donate". */
        const val DONATE: String = "modmenu.donate"

        /** `modmenu.flattr`, shown as "Flattr". */
        const val FLATTR: String = "modmenu.flattr"

        /** `modmenu.github_releases`, shown as "GitHub Releases". */
        const val GITHUB_RELEASES: String = "modmenu.github_releases"

        /** `modmenu.github_sponsors`, shown as "GitHub Sponsors". */
        const val GITHUB_SPONSORS: String = "modmenu.github_sponsors"

        /** `modmenu.kofi`, shown as "Ko-fi". */
        const val KOFI: String = "modmenu.kofi"

        /** `modmenu.liberapay`, shown as "Liberapay". */
        const val LIBERAPAY: String = "modmenu.liberapay"

        /** `modmenu.mastodon`, shown as "Mastodon". */
        const val MASTODON: String = "modmenu.mastodon"

        /** `modmenu.modrinth`, shown as "Modrinth". */
        const val MODRINTH: String = "modmenu.modrinth"

        /** `modmenu.opencollective`, shown as "Open Collective". */
        const val OPEN_COLLECTIVE: String = "modmenu.opencollective"

        /** `modmenu.patreon`, shown as "Patreon". */
        const val PATREON: String = "modmenu.patreon"

        /** `modmenu.paypal`, shown as "PayPal". */
        const val PAYPAL: String = "modmenu.paypal"

        /** `modmenu.reddit`, shown as "Reddit". */
        const val REDDIT: String = "modmenu.reddit"

        /** `modmenu.twitch`, shown as "Twitch". */
        const val TWITCH: String = "modmenu.twitch"

        /** `modmenu.twitter`, shown as "X (Twitter)". */
        const val TWITTER: String = "modmenu.twitter"

        /** `modmenu.wiki`, shown as "Wiki". */
        const val WIKI: String = "modmenu.wiki"

        /** `modmenu.youtube`, shown as "YouTube". */
        const val YOUTUBE: String = "modmenu.youtube"
    }
}
