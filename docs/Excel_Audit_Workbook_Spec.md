# Excel Audit Workbook Specification

This document outlines the structure and purpose of the multi-tab Excel audit workbook used for the application dependency matrix process. Each sheet corresponds to a phase of the data pipeline and captures how claims evolve from raw inputs to finalized dependencies.

## Raw Claims
- **Purpose:** List all claims exactly as collected from each source.
- **Contents:** Source name, entity, property/relationship, value, timestamps or metadata.
- **Notes:** No filtering or normalization. Provides traceability of the original data.

## Normalization Mapping
- **Purpose:** Document how names and values from different sources are standardized.
- **Contents:** Tables mapping original identifiers and attribute names to their normalized forms. Highlight significant changes or transformation rules (case-folding, ontology lookups, etc.).

## Alias/Group Resolution
- **Purpose:** Show which identifiers were merged as referring to the same entity.
- **Contents:** For each canonical entity ID, list all aliases from the sources and note how the merge decision was made. Include example merged claims when useful.

## Normalized Claims
- **Purpose:** Provide the cleaned set of claims ready for analysis.
- **Contents:** Normalized entity IDs, properties, values, source attribution, and claim IDs. Include generated negative claims where applicable.

## Initial Confidence Aggregation
- **Purpose:** Display naive vote-based confidence before truth discovery.
- **Contents:** For each conflict group, list competing values with vote counts and the naive winner. Include any initial trust scores if used as tie-breakers.

## LTM Iterations
- **Purpose:** Trace how claim probabilities and source trustworthiness scores evolve during the EM-based Latent Truth Model.
- **Contents:** Tables of trust scores per source and credibility per claim for each iteration. Graphs may visualize convergence.

## Final Consolidated Dependencies
- **Purpose:** Present the final believed-true claims after conflict resolution.
- **Contents:** Accepted claims organized by dependency order, source support for each claim, and optionally rejected/false claims.

## Data Coverage
- **Purpose:** Summarize gaps and weak points in the data.
- **Contents:** Coverage metrics for each entity and attribute, contribution by source, conflict density, and counts of negative claims. Graphs can highlight areas with little or no evidence.

## Optional: Negative Claims Generation
- **Purpose:** Provide transparency for any generated negative claims.
- **Contents:** Cases where a source could have reported a value but did not, resulting in a negative claim. Note whether these claims were ultimately accepted or rejected.

This workbook allows auditors to follow the entire pipeline from raw data collection to final dependency conclusions, ensuring transparency and providing insight into the quality and coverage of the integrated data.
