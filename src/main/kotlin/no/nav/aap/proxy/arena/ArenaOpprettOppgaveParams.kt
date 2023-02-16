package no.nav.aap.proxy.arena

import no.nav.aap.api.felles.Fødselsnummer

data class ArenaOpprettOppgaveParams(val fnr: Fødselsnummer, val enhet: String, val tittel: String, val titler: List<String>)