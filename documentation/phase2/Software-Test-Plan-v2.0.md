# GizmoHub — Software Test Plan v2.0

| Field | Value |
|---|---|
| Version | 2.0 |
| Phase | Phase 2 — White-box testing |
| Date | ________ |
| Authors | Member 1 ____ · Member 2 ____ · Member 3 ____ · Member 4 ____ |
| Application build | git commit `________` |
| Status | Draft / Approved |

## 1. Introduction and scope
Objective of this phase: ________

In scope (requirement IDs): ________
Out of scope: ________

## 2. Test items
| Item | Location | Version |
|---|---|---|
| Backend | `backend/` | commit ____ |
| Frontend | `frontend/` | commit ____ |
| Requirements | `documentation/requirements/SRS.md` | 1.0 |

## 3. Features to be tested
| Module | Owner | Requirements | Techniques |
|---|---|---|---|
| Authentication | Member 1 | AUTH-01..08 | |
| Product Management | Member 2 | PROD-01..08 | |
| Shopping Cart | Member 3 | CART-01..08 | |
| Checkout & Orders | Member 4 | ORDER-01..12 | |

## 4. Approach
Control-flow graphs, statement / decision / condition / MCC coverage, basis-path and LCSAJ analysis, data-flow (def-use) testing and PIT mutation testing on the methods listed in `phase2/white-box-guide.md`. Coverage measured with JaCoCo (`./mvnw test`), mutation score with PIT. Record a baseline BEFORE adding tests, then results AFTER the test-suite enhancement.

## 5. Entry and exit criteria
| | Criterion |
|---|---|
| Entry | Application builds with `./mvnw test`; seed data loads; test cases reviewed |
| Exit | ________ (e.g. all planned cases executed, no open Critical/High defect, targets below met or deviation explained) |
| Suspension | ________ |

## 6. Targets (set BEFORE execution)
| Metric | Target | Source of measurement |
|---|---|---|
| | | |

## 7. Environment
| Item | Value |
|---|---|
| OS | |
| JDK | (`java -version`) |
| Browser | |
| Run command | `cd backend && ./mvnw spring-boot:run` |

## 8. Test deliverables
- Test cases: `documentation/test-cases/*.csv`
- Evidence: `documentation/evidence/<phase>/`
- Defect reports: `documentation/phase3/defect-report-template.md` copies

## 9. Schedule and responsibilities
| Task | Owner | Planned date | Done date |
|---|---|---|---|
| | | | |

## 10. Risks
| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| | | | |

## 11. Changes from previous version
________

## 12. Approval
| Name | Role | Signature / date |
|---|---|---|
| | | |
