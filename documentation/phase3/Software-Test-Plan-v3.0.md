# GizmoHub — Software Test Plan v3.0

| Field | Value |
|---|---|
| Version | 3.0 |
| Phase | Phase 3 — Integration, system, non-functional and regression testing |
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
Integration tests across module interfaces (Spring Boot Test + MockMvc), end-to-end system scenarios in the browser, non-functional checks NFR-01..03 with the acceptance criteria in the SRS, and regression testing after each documented fix.

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
