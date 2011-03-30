package org.protempa.query.handler.xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protempa.FinderException;
import org.protempa.KnowledgeSource;
import org.protempa.ProtempaException;
import org.protempa.proposition.Proposition;
import org.protempa.proposition.PropositionCheckedVisitor;
import org.protempa.proposition.UniqueIdentifier;
import org.protempa.query.handler.WriterQueryResultsHandler;

public class XmlQueryResultsHandler extends WriterQueryResultsHandler {

    private KnowledgeSource knowledgeSource;

    public XmlQueryResultsHandler(OutputStream out) {
        super(out);
    }

    public XmlQueryResultsHandler(File file) throws IOException {
        super(file);
    }

    public XmlQueryResultsHandler(String fileName) throws IOException {
        super(fileName);
    }

    public XmlQueryResultsHandler(Writer out) {
        super(out);
    }

    @Override
    public void finish() throws FinderException {
        try {
            this.write("</patients>");
            this.flush();
        } catch (IOException ioe) {
            throw new FinderException(ioe);
        }
        super.finish();
    }

    @Override
    public void init(KnowledgeSource knowledgeSource) throws FinderException {
        super.init(knowledgeSource);
        this.knowledgeSource = knowledgeSource;
        try {
            this.write("<patients>");
            this.flush();
        } catch (IOException ioe) {
            throw new FinderException(ioe);
        }
    }

    private List<Proposition> createReferenceList(List<UniqueIdentifier> uids,
            Map<UniqueIdentifier, Proposition> references) {
        List<Proposition> propositions = new ArrayList<Proposition>();
        for (UniqueIdentifier uid : uids) {
            Proposition refProp = references.get(uid);
            if (refProp != null) {
                propositions.add(refProp);
            }
        }
        return propositions;
    }

    private List<Proposition> filterHandled(List<Proposition> propositions,
            Set<Proposition> handled) {
        List<Proposition> filtered = new ArrayList<Proposition>();
        for (Proposition proposition : propositions) {
            if (!handled.contains(proposition)) {
                filtered.add(proposition);
            }
        }
        return filtered;
    }

    private void handleProposition(Set<Proposition> handled,
            Map<Proposition, List<Proposition>> derivations,
            Map<UniqueIdentifier, Proposition> references,
            Proposition proposition, PropositionCheckedVisitor visitor)
            throws ProtempaException {
        if (!handled.contains(proposition)) {
            handled.add(proposition);
            try {
                this.write("<proposition id=\"" + proposition.getId() + "\">");
                proposition.acceptChecked(visitor);

                Set<String> refNames = proposition.getReferenceNames();
                if (refNames != null && (!refNames.isEmpty())) {
                    this.write("<references>");
                    for (String refName : refNames) {
                        List<UniqueIdentifier> uids = proposition
                                .getReferences(refName);
                        List<Proposition> refProps = createReferenceList(uids,
                                references);
                        if (!refProps.isEmpty()) {
                            List<Proposition> filteredReferences = filterHandled(
                                    refProps, handled);
                            if (!filteredReferences.isEmpty()) {
                                this.write("<reference type=\"" + refName
                                        + "\">");
                                for (Proposition refProp : filteredReferences) {
                                    handleProposition(handled, derivations,
                                            references, refProp, visitor);
                                }
                                this.write("</reference>");
                            }
                        }
                    }
                    this.write("</references>");
                }

                List<Proposition> derivedPropositions = derivations
                        .get(proposition);
                if (derivedPropositions != null) {
                    this.write("<derivations>");
                    for (Proposition derivedProposition : derivedPropositions) {
                        handleProposition(handled, derivations, references,
                                derivedProposition, visitor);
                    }
                    this.write("</derivations>");
                }
                this.write("</proposition>");
                this.flush();
            } catch (IOException e) {
                throw new XmlQueryResultsHandlerException(e);
            }
        }
    }

    @Override
    public void handleQueryResult(String key, List<Proposition> propositions,
            Map<Proposition, List<Proposition>> derivations,
            Map<UniqueIdentifier, Proposition> references)
            throws FinderException {
        try {
            Set<Proposition> handled = new HashSet<Proposition>();
            Set<Proposition> propositionsAsSet = new HashSet<Proposition>();
            List<Proposition> propositionsCopy = new ArrayList<Proposition>(
                    propositionsAsSet);

            PropositionCheckedVisitor visitor = new XmlPropositionVisitor(
                    this.knowledgeSource, this);

            Proposition patientAll = null;
            for (Proposition p : propositions) {
                if (p.getId().equals("PatientAll")) {
                    patientAll = p;
                    break;
                }
            }

            this.write("<patient id=\"" + key + "\">");
            if (patientAll != null) {
                handleProposition(handled, derivations, references, patientAll,
                        visitor);
            }
            for (Proposition proposition : propositionsCopy) {
                handleProposition(handled, derivations, references,
                        proposition, visitor);
            }
            this.write("</patient>");
            this.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProtempaException e) {
            e.printStackTrace();
        }
    }
}
