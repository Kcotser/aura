/**
 * Reporting bounded context.
 *
 * <p>Manages the lifecycle of an incident report: Draft → Edited → Approved → Exported.
 * Aggregates evidence metadata and AI analysis results into a structured
 * report that can be exported as PDF for legal purposes.
 *
 * TODO: implement after hackathon
 */
@org.springframework.modulith.ApplicationModule(displayName = "Reporting")
package com.aura.reporting;
