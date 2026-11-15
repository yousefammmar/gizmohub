# Phase 1 analysis — Module: ________  · Owner: ________

> Copy this file once per team member (e.g. `phase1-auth-member1.md`). Facts about inputs and rules are in
> `black-box-test-basis.md`; this file holds **your** analysis. Leave "Actual result" columns empty until executed.

## A. Equivalence Partitioning
### A.1 Unidimensional
| Input | Input domain | Class ID | Valid / Invalid | Class definition | Representative value | Expected result | Rationale |
|---|---|---|---|---|---|---|---|
| | | EC1 | Valid | | | | |
| | | EC2 | Invalid | | | | |

### A.2 Multidimensional
| Combination ID | Classes combined | Representative inputs | Expected result | Why this combination |
|---|---|---|---|---|
| | | | | |

## B. Boundary Value Analysis
| Input | Boundary | Inclusive / exclusive | min-1 | min | min+1 | max-1 | max | max+1 |
|---|---|---|---|---|---|---|---|---|
| | | | | | | | | |
Expected result for each value:

| Value | Expected result | Test case ID |
|---|---|---|
| | | |

## C. Cause–Effect Graph
- Causes: C1 … · C2 … · C3 …
- Effects: E1 … · E2 …
- Relations (AND / OR / NOT): E1 = …
- Constraints (Exclusive / Inclusive / One-and-only-one / Requires / Masks): …
- Feasible combinations: …
- Infeasible combinations and why: …
- Graph image: `../evidence/phase1/<module>-ceg.png`

## D. Decision Table
| Conditions / Actions | R1 | R2 | R3 | R4 | R5 | R6 |
|---|---|---|---|---|---|---|
| C1 | | | | | | |
| C2 | | | | | | |
| C3 | | | | | | |
| A1 | | | | | | |
| A2 | | | | | | |
- Possible values per condition: …
- Infeasible rules removed: … (reason)
- Simplifications (merged "don't care" rules) and justification: …

## E. Derived test cases
List the IDs you added to `../test-cases/black-box-test-cases.csv` (minimum 5 distinct per member): …
