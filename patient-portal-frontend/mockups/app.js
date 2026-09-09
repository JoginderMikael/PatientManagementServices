/* Northstar Health interactive mockups — synthetic data only. */
(function () {
  "use strict";

  const app = document.getElementById("app");
  const dialogRoot = document.getElementById("dialog-root");
  const toastRoot = document.getElementById("toast-root");

  const state = {
    view: "staff",
    role: "CLINICIAN",
    navOpen: false,
    indexOpen: false,
    currentPatient: {
      id: "pat-1047",
      name: "Amara Njeri",
      mrn: "MRN-1047-62",
      dob: "14 Feb 1988",
      status: "Active",
      access: "Standard access",
    },
  };

  const roleLabels = {
    ADMIN: "Administrator",
    CLINICIAN: "Clinician",
    NURSE: "Nurse",
    REGISTRATION_STAFF: "Registration staff",
    RECEPTIONIST: "Receptionist",
    BILLING_STAFF: "Billing staff",
    PHARMACIST: "Pharmacist",
    LAB_STAFF: "Laboratory staff",
    PRIVACY_OFFICER: "Privacy officer",
    AUDITOR: "Auditor",
    ANALYST: "Analyst",
  };

  const patientNav = [
    ["Overview", "#/patient/overview", "OV"],
    ["Appointments", "#/patient/appointments", "AP"],
    ["Health records", "#/patient/records", "HR"],
    ["Bills & payments", "#/patient/billing", "BP"],
    ["Profile & proxy access", "#/patient/access", "PA"],
  ];

  const staffNav = [
    [
      "Work queue",
      "#/staff/work-queue",
      "WQ",
      [
        "ADMIN",
        "CLINICIAN",
        "NURSE",
        "RECEPTIONIST",
        "BILLING_STAFF",
        "PHARMACIST",
        "LAB_STAFF",
      ],
    ],
    [
      "Patients",
      "#/staff/patients",
      "PT",
      ["ADMIN", "CLINICIAN", "REGISTRATION_STAFF"],
    ],
    [
      "Schedule",
      "#/staff/schedule",
      "SC",
      ["ADMIN", "CLINICIAN", "REGISTRATION_STAFF"],
    ],
    [
      "Clinical chart",
      "#/staff/patients/pat-1047/summary",
      "CC",
      ["ADMIN", "CLINICIAN", "NURSE"],
    ],
    [
      "Billing",
      "#/staff/patients/pat-1047/billing",
      "BL",
      ["ADMIN", "BILLING_STAFF"],
    ],
    ["Insurance", "#/staff/insurance", "IN", ["ADMIN", "BILLING_STAFF"]],
    ["Pharmacy", "#/staff/pharmacy", "RX", ["ADMIN", "PHARMACIST"]],
    ["Inventory", "#/staff/inventory", "IV", ["ADMIN", "PHARMACIST"]],
    [
      "Notifications",
      "#/staff/notifications",
      "NT",
      ["ADMIN", "CLINICIAN", "REGISTRATION_STAFF", "BILLING_STAFF"],
    ],
    ["Compliance", "#/staff/compliance", "CP", ["ADMIN", "PRIVACY_OFFICER"]],
    ["Audit evidence", "#/staff/audit", "AU", ["ADMIN", "AUDITOR"]],
    ["Reports", "#/staff/reports", "RP", ["ADMIN", "ANALYST"]],
  ];

  const allScreens = [
    [
      "Entry states",
      [
        ["Sign in", "#/login"],
        ["Access denied", "#/forbidden"],
        ["System states", "#/states"],
      ],
    ],
    ["Patient portal", patientNav.map(([label, href]) => [label, href])],
    [
      "Staff operations",
      [
        ["Work queue", "#/staff/work-queue"],
        ["Patient registry", "#/staff/patients"],
        ["Patient summary", "#/staff/patients/pat-1047/summary"],
        ["Demographics", "#/staff/patients/pat-1047/demographics"],
        ["Encounters", "#/staff/patients/pat-1047/encounters"],
        ["Medications", "#/staff/patients/pat-1047/medications"],
        ["Laboratory", "#/staff/patients/pat-1047/labs"],
        ["Patient billing", "#/staff/patients/pat-1047/billing"],
        ["Schedule", "#/staff/schedule"],
        ["Insurance", "#/staff/insurance"],
        ["Pharmacy", "#/staff/pharmacy"],
        ["Inventory", "#/staff/inventory"],
        ["Notifications", "#/staff/notifications"],
        ["Compliance", "#/staff/compliance"],
        ["Audit evidence", "#/staff/audit"],
        ["Reports", "#/staff/reports"],
      ],
    ],
  ];

  function route() {
    return (location.hash || "#/staff/work-queue").slice(1);
  }

  function status(text, tone = "neutral") {
    return `<span class="status ${tone}">${text}</span>`;
  }

  function button(text, cls = "", action = "notice", attrs = "") {
    return `<button class="button ${cls}" type="button" data-action="${action}" ${attrs}>${text}</button>`;
  }

  function alertBox(title, text, tone = "info") {
    const icons = { info: "i", warning: "!", danger: "×", success: "✓" };
    return `<div class="alert ${tone}" role="${tone === "danger" ? "alert" : "status"}"><span class="alert-icon" aria-hidden="true">${icons[tone]}</span><div><strong>${title}</strong>${text}</div></div>`;
  }

  function pageHeader(eyebrow, title, description, actions = "") {
    return `<header class="page-header"><div class="page-header-text"><p class="eyebrow">${eyebrow}</p><h1 tabindex="-1">${title}</h1>${description ? `<p class="page-description">${description}</p>` : ""}</div>${actions ? `<div class="page-actions">${actions}</div>` : ""}</header>`;
  }

  function card(title, body, subtitle = "", cls = "") {
    return `<section class="card ${cls}"><header class="card-header"><div><h2>${title}</h2>${subtitle ? `<p>${subtitle}</p>` : ""}</div></header><div class="card-body">${body}</div></section>`;
  }

  function metric(label, value, detail, tone = "accent") {
    return `<section class="card metric ${tone}"><div class="metric-label">${label}</div><div class="metric-value">${value}</div><div class="metric-detail">${detail}</div></section>`;
  }

  function table(caption, headers, rows) {
    return `<div class="table-wrap" tabindex="0" role="region" aria-label="${caption}"><table><caption>${caption}</caption><thead><tr>${headers.map((h) => `<th scope="col">${h}</th>`).join("")}</tr></thead><tbody>${rows.map((row) => `<tr>${row.map((cell) => `<td>${cell}</td>`).join("")}</tr>`).join("")}</tbody></table></div>`;
  }

  function field(label, control, help = "") {
    return `<div class="field"><label>${label}${control}</label>${help ? `<p class="field-help">${help}</p>` : ""}</div>`;
  }

  function input(type, value = "", attrs = "") {
    return `<input type="${type}" value="${value}" ${attrs} />`;
  }

  function select(options, selected = "") {
    return `<select>${options.map((o) => `<option${o === selected ? " selected" : ""}>${o}</option>`).join("")}</select>`;
  }

  function patientTabs(active) {
    const tabs = [
      ["Summary", "summary"],
      ["Demographics", "demographics"],
      ["Encounters", "encounters"],
      ["Medications", "medications"],
      ["Labs", "labs"],
      ["Billing", "billing"],
    ];
    return `<nav class="tabs" aria-label="Patient chart sections">${tabs.map(([label, slug]) => `<button type="button" class="tab ${active === slug ? "active" : ""}" data-route="#/staff/patients/pat-1047/${slug}">${label}</button>`).join("")}</nav>`;
  }

  function shell(content, patientContext = false) {
    const current = route();
    const nav =
      state.view === "patient"
        ? patientNav
        : staffNav.filter(([, , , roles]) => roles.includes(state.role));
    const identity =
      state.view === "patient"
        ? ["Samira Kamau", "Patient"]
        : ["Dr. Elias Okoth", roleLabels[state.role]];
    return `<div class="app-shell">
      <header class="topbar">
        <button class="menu-button" type="button" data-action="toggle-nav" aria-label="Open navigation" aria-expanded="${state.navOpen}">☰</button>
        <div class="brand"><span class="brand-mark" aria-hidden="true">N+</span><span><strong>Northstar Health</strong><small>Patient management</small></span></div>
        <span class="env-badge">SYNTHETIC · UAT</span>
        <div class="topbar-spacer"></div>
        <div class="view-switch" aria-label="Mockup surface"><button class="${state.view === "patient" ? "active" : ""}" data-view="patient">Portal view</button><button class="${state.view === "staff" ? "active" : ""}" data-view="staff">Staff view</button></div>
        <button class="icon-button" type="button" data-route="#/staff/notifications" aria-label="Notifications">3</button>
        <div class="user-block"><span class="avatar" aria-hidden="true">${identity[0]
          .split(" ")
          .map((x) => x[0])
          .join("")
          .slice(
            0,
            2,
          )}</span><span class="user-meta"><strong>${identity[0]}</strong><span>${identity[1]}</span></span></div>
        ${
          state.view === "staff"
            ? `<label class="sr-only" for="role-preview">Preview role</label><select id="role-preview" class="role-select" data-role>${Object.entries(
                roleLabels,
              )
                .map(
                  ([key, label]) =>
                    `<option value="${key}"${key === state.role ? " selected" : ""}>${label}</option>`,
                )
                .join("")}</select>`
            : ""
        }
        <button class="signout" type="button" data-route="#/login">Sign out</button>
      </header>
      <div class="body-grid">
        <aside class="sidebar ${state.navOpen ? "open" : ""}" aria-label="Primary navigation">
          <p class="nav-label">${state.view === "patient" ? "My health" : "Operations"}</p>
          <ul class="nav-list">${nav.map(([label, href, icon]) => `<li class="nav-item"><button class="nav-link ${current === href.slice(1) || (label === "Clinical chart" && current.includes("/staff/patients/pat-1047/")) ? "active" : ""}" data-route="${href}"><span class="nav-icon" aria-hidden="true">${icon}</span>${label}</button></li>`).join("")}</ul>
          <hr class="nav-divider" />
          <button class="nav-link" data-action="open-index"><span class="nav-icon" aria-hidden="true">••</span>All mockup screens</button>
          <div class="nav-help"><strong>UAT environment</strong>Only synthetic information is shown. Never enter real patient data.</div>
        </aside>
        <div class="workspace">
          ${patientContext ? patientBanner() : ""}
          <main id="main-content" class="content">${content}</main>
        </div>
      </div>
      ${screenIndex()}
    </div>`;
  }

  function patientBanner() {
    const p = state.currentPatient;
    return `<section class="patient-banner" aria-label="Current patient context"><div class="patient-banner-main"><strong>${p.name}</strong><span>${p.mrn} · ${p.access}</span></div><dl class="patient-fact"><dt>Date of birth</dt><dd>${p.dob}</dd></dl><dl class="patient-fact"><dt>Lifecycle</dt><dd>${status(p.status, "success")}</dd></dl><dl class="patient-fact"><dt>Allergies</dt><dd class="text-danger">Penicillin · confirmed</dd></dl><div class="spacer"></div>${button("Change patient", "small", "change-patient")}</section>`;
  }

  function screenIndex() {
    return `<div class="screen-index"><div class="screen-index-panel" ${state.indexOpen ? "" : "hidden"}><h2>All mockup screens</h2>${allScreens.map(([group, screens]) => `<div class="screen-group"><strong>${group}</strong><div class="screen-grid">${screens.map(([label, href]) => `<button data-route="${href}">${label}</button>`).join("")}</div></div>`).join("")}</div>${button(state.indexOpen ? "Close screens" : "All screens", "primary", "open-index")}</div>`;
  }

  function loginPage() {
    return `<main class="login-layout" id="main-content"><section class="login-brand"><div class="brand"><span class="brand-mark">N+</span><span><strong>Northstar Health</strong><small>Patient management</small></span></div><div class="login-message"><p class="eyebrow" style="color:#8ad8d5">Secure care workspace</p><h1>One place for safer, coordinated care.</h1><p>Access your care activity or the clinical tools assigned to your role.</p></div><div class="login-foot">Authorized use only · Activity is audited · UAT uses synthetic information</div></section><section class="login-form-wrap"><form class="login-form" data-submit="login"><h2>Sign in</h2><p>Use the account issued by your facility.</p>${alertBox("Training environment", "Do not enter production credentials or real patient information.", "info")}${field('Email or staff ID <span class="required">*</span>', input("text", "clinician.demo", 'autocomplete="username" required'))}${field('Password <span class="required">*</span>', input("password", "Northstar-2026", 'autocomplete="current-password" required'))}${field("Sign in to", select(["Staff operations", "Patient portal"]))}<label class="check-row"><input type="checkbox" /> <span>Keep me signed in on this trusted device</span></label><div class="form-actions">${button("Need help?", "ghost", "notice")}${button("Sign in", "primary", "submit")}</div></form></section></main>${screenIndex()}`;
  }

  function forbiddenPage() {
    return `<main class="forbidden" id="main-content"><section class="forbidden-card"><div class="forbidden-mark" aria-hidden="true">!</div><p class="eyebrow">Access denied · 403</p><h1>You do not have access to this page</h1><p>Your session is still active. This page requires a role or patient relationship that is not assigned to your account.</p><div class="privacy-note">Request ID: <span class="mono">REQ-UAT-4C92D</span>. Share this ID with support; do not include patient details.</div><div class="forbidden-actions">${button("Go to my start page", "primary", "home")}${button("Sign out", "", "logout")}</div></section></main>${screenIndex()}`;
  }

  function statesPage() {
    return shell(
      pageHeader(
        "Shared patterns",
        "System and data states",
        "Reusable references for privacy-safe loading, empty, stale, validation, conflict and service interruption states.",
      ) +
        `<div class="grid two">${card("Initial loading", `<div class="skeleton" aria-label="Loading content"><span></span><span></span><span></span></div><p class="muted">Patient information is loading…</p>`)}${card("Empty result", `<div class="empty-state"><div class="empty-state-mark">0</div><h3>No appointments found</h3><p>There are no appointments matching the current filters.</p>${button("Clear filters", "small", "notice")}</div>`)}${card("Stale information", alertBox("Update available", "You can continue reading this information while the latest version loads.", "info") + button("Refresh now", "small", "notice"))}${card("Validation summary", alertBox("Review 2 fields", "Appointment date must be in the future. Duration must be between 5 and 480 minutes.", "danger"))}${card("Workflow conflict · 409", alertBox("The task changed", "Another staff member claimed this task. Review the current owner before continuing.", "warning") + button("Reload current state", "small", "notice"))}${card("Service unavailable · 503", alertBox("This service is temporarily unavailable", "No changes were made. Try again in a few minutes. Request ID: REQ-UAT-7C40.", "danger") + button("Try again", "small", "notice"))}</div>`,
    );
  }

  function patientOverview() {
    const content =
      pageHeader(
        "Patient portal",
        "Good morning, Samira",
        "Your next steps and recent care activity.",
        button("Request appointment", "primary", "appointment-form"),
      ) +
      alertBox(
        "Medication pickup ready",
        "Your prescription is ready at Westlands Pharmacy until 13 September.",
        "info",
      ) +
      `<div class="grid four">${metric("Next appointment", "12 Sep", "09:30 · Cardiology")}${metric("Record request", "In review", "Submitted 06 Sep", "warning")}${metric("Amount due", "KES 4,850", "Due 20 Sep", "warning")}${metric("Proxy access", "1 active", "Records only")}</div>
      <div class="grid main-aside section-gap"><div class="stack">
        ${card("Upcoming appointment", `<div class="definition-list"><div><dt>Date and time</dt><dd>Saturday, 12 September 2026 · 09:30 EAT</dd></div><div><dt>Clinician</dt><dd>Dr. Wanjiku Muriithi · Cardiology</dd></div><div><dt>Location</dt><dd>Westlands Medical Centre · Clinic 3</dd></div><div><dt>Status</dt><dd>${status("Confirmed", "success")}</dd></div></div><div class="form-actions">${button("View details", "small", "route-appointments")}${button("Add to calendar", "small", "notice")}</div>`)}
        ${card("Recent activity", `<ul class="timeline"><li><strong>Payment received · KES 2,000</strong><span>08 Sep 2026 · Receipt PMT-28814</span></li><li><strong>Record request submitted</strong><span>06 Sep 2026 · Clinical summary · In review</span></li><li><strong>Appointment confirmed</strong><span>04 Sep 2026 · Cardiology clinic</span></li></ul>`)}
      </div><div class="stack">${card("Care reminders", `<p><strong>Annual wellness visit</strong></p><p class="muted">Due by 31 October 2026.</p>${button("Request appointment", "small primary", "appointment-form")}`)}${card("Privacy at a glance", `<p class="muted">Your information is visible only to permitted care teams and proxies you authorize.</p>${button("Manage proxy access", "small", "route-access")}`)}</div></div>`;
    return shell(content);
  }

  function patientAppointments() {
    const rows = [
      [
        '<span class="row-title">Cardiology review</span><span class="row-subtitle">Dr. Wanjiku Muriithi</span>',
        '12 Sep 2026<br><span class="muted">09:30 EAT</span>',
        "Westlands · Clinic 3",
        status("Confirmed", "success"),
        button("Details", "small", "notice"),
      ],
      [
        '<span class="row-title">Annual wellness visit</span><span class="row-subtitle">Preferred morning</span>',
        "Requested 08 Sep",
        "Any outpatient clinic",
        status("Pending", "warning"),
        button("Cancel request", "small danger", "cancel-appointment"),
      ],
      [
        '<span class="row-title">Telemedicine follow-up</span><span class="row-subtitle">Dr. A. Mwangi</span>',
        '21 Aug 2026<br><span class="muted">14:00 EAT</span>',
        "Virtual consultation",
        status("Completed", "neutral"),
        button("Summary", "small", "notice"),
      ],
    ];
    return shell(
      pageHeader(
        "Patient portal",
        "Appointments",
        "Times are shown in East Africa Time (EAT, UTC+3).",
        button("Request appointment", "primary", "appointment-form"),
      ) +
        `<div class="tabs" role="tablist"><button class="tab active" role="tab">Upcoming & requests</button><button class="tab" role="tab">Past appointments</button></div><div class="tab-panel card flush">${table("Your appointments and requests", ["Appointment", "Date & time", "Location", "Status", "Actions"], rows)}</div>`,
    );
  }

  function patientRecords() {
    const rows = [
      [
        "Clinical summary",
        "06 Sep 2026",
        status("In review", "warning"),
        "—",
        button("View request", "small", "notice"),
      ],
      [
        "Laboratory results",
        "16 Aug 2026",
        status("Ready", "success"),
        "Available until 16 Oct",
        button("Download .txt", "small primary", "download-record"),
      ],
      [
        "Visit summary",
        "02 Jul 2026",
        status("Downloaded", "neutral"),
        "Expired",
        button("Request again", "small", "record-form"),
      ],
    ];
    return shell(
      pageHeader(
        "Patient portal",
        "Health records",
        "Request and securely download copies of your health information.",
        button("Request records", "primary", "record-form"),
      ) +
        alertBox(
          "Downloads are sensitive",
          "Use a trusted device. Downloaded files may contain private health information and are not saved by this portal.",
          "warning",
        ) +
        `<div class="card flush">${table("Record requests", ["Records requested", "Submitted", "Status", "Availability", "Actions"], rows)}</div>`,
    );
  }

  function patientBilling() {
    const rows = [
      [
        "INV-2026-0812",
        "Cardiology consultation",
        "28 Aug 2026",
        "KES 6,850",
        "KES 2,000",
        "<strong>KES 4,850</strong>",
        status("Part paid", "warning"),
      ],
      [
        "INV-2026-0631",
        "Laboratory services",
        "16 Aug 2026",
        "KES 3,200",
        "KES 3,200",
        "KES 0",
        status("Paid", "success"),
      ],
    ];
    return shell(
      pageHeader(
        "Patient portal",
        "Bills and payments",
        "Review balances and submit a secure payment intent.",
        button("Make a payment", "primary", "payment-form"),
      ) +
        `<div class="grid three">${metric("Outstanding", "KES 4,850", "Across 1 invoice", "warning")}${metric("Paid this year", "KES 18,420", "5 successful payments")}${metric("Insurance pending", "KES 12,600", "Claim CLM-02418")}</div><div class="card flush section-gap">${table("Invoices", ["Invoice", "Description", "Issued", "Amount", "Paid", "Outstanding", "Status"], rows)}</div>`,
    );
  }

  function patientAccess() {
    return shell(
      pageHeader(
        "Patient portal",
        "Profile and proxy access",
        "Review contact information and people who can act on your behalf.",
        button("Add proxy access", "primary", "proxy-form"),
      ) +
        `<div class="grid main-aside"><div class="stack">${card("Profile", `<dl class="definition-list"><div><dt>Full name</dt><dd>Samira Achieng Kamau</dd></div><div><dt>Date of birth</dt><dd>23 April 1994</dd></div><div><dt>Email</dt><dd>s•••••@example.test</dd></div><div><dt>Mobile</dt><dd>+254 7•• ••• 482</dd></div><div><dt>Preferred contact</dt><dd>SMS</dd></div><div><dt>Language</dt><dd>English</dd></div></dl><div class="form-actions">${button("Request profile update", "small", "notice")}</div>`)}${card("Active proxy grants", table("People with access", ["Proxy", "Relationship", "Scope", "Expires", "Action"], [["Amina Kamau", "Parent", status("Records only", "info"), "30 Nov 2026", button("Revoke", "small danger", "revoke-proxy")]]), "One active grant", "flush")}</div>${card("Access boundaries", `<p><strong>Choose the narrowest scope needed.</strong></p><p class="muted">A proxy may receive Records, Appointments, or Payments access. Each grant can be revoked independently.</p><div class="privacy-note">The care team may restrict access where required by law or clinical safety policy.</div>`)}</div>`,
    );
  }

  function workQueue() {
    const rows = [
      [
        status("Overdue · urgent", "danger"),
        '<span class="row-title">Review critical potassium result</span><span class="row-subtitle">Amara Njeri · MRN-1047-62</span>',
        "Laboratory",
        "08:15 EAT",
        "Unassigned",
        `<div class="row-actions">${button("Claim", "small primary", "claim-task")}${button("Open", "small", "patient-summary")}</div>`,
      ],
      [
        status("Due soon", "warning"),
        '<span class="row-title">Medication reconciliation</span><span class="row-subtitle">Daniel Otieno · MRN-1633-20</span>',
        "Clinical",
        "10:30 EAT",
        "Dr. E. Okoth",
        button("Open", "small", "notice"),
      ],
      [
        status("On track", "success"),
        '<span class="row-title">Complete discharge summary</span><span class="row-subtitle">Linet Wambui · MRN-1022-91</span>',
        "Clinical",
        "14:00 EAT",
        "Dr. E. Okoth",
        button("Open", "small", "notice"),
      ],
      [
        status("New", "info"),
        '<span class="row-title">Confirm follow-up booking</span><span class="row-subtitle">Kofi Mensah · MRN-1840-05</span>',
        "Operations",
        "Tomorrow",
        "Reception queue",
        button("Open", "small", "notice"),
      ],
    ];
    return shell(
      pageHeader(
        "Staff operations",
        "Work queue",
        "Urgent and overdue work appears first. Queue times use EAT.",
        button("Create task", "primary", "task-form"),
      ) +
        alertBox(
          "1 overdue safety task",
          "Review the critical result before beginning routine queue work.",
          "danger",
        ) +
        `<div class="grid four">${metric("Assigned to me", "7", "2 due in the next hour", "warning")}${metric("Unassigned", "12", "Across permitted queues")}${metric("Overdue", "1", "Clinical safety review", "warning")}${metric("Completed today", "9", "Last completed 08:42")}</div>
      <div class="toolbar section-gap">${field("Queue", select(["Clinical", "Nursing", "Operations", "All permitted"], "All permitted"))}${field("Status", select(["Open", "Claimed", "Completed"], "Open"))}${field("Due", select(["Any time", "Today", "Overdue"], "Today"))}<div class="field grow">${field("Search tasks", input("search", "", 'placeholder="Task, patient or MRN"'))}</div>${button("Apply filters", "primary", "notice")}</div><section class="card flush">${table("Task queue", ["Priority", "Task", "Queue", "Due", "Owner", "Actions"], rows)}</section>`,
    );
  }

  function patientsPage() {
    const rows = [
      [
        '<span class="row-title">Amara Njeri</span><span class="row-subtitle">MRN-1047-62</span>',
        "14 Feb 1988",
        "+254 7•• ••• 901",
        status("Active", "success"),
        button("Open record", "small primary", "patient-summary"),
      ],
      [
        '<span class="row-title">Daniel Otieno</span><span class="row-subtitle">MRN-1633-20</span>',
        "09 Jun 1972",
        "+254 7•• ••• 744",
        status("Active", "success"),
        button("Open record", "small", "patient-summary"),
      ],
      [
        '<span class="row-title">Linet Wambui</span><span class="row-subtitle">MRN-1022-91</span>',
        "21 Nov 2001",
        "+254 7•• ••• 302",
        status("Inactive", "neutral"),
        button("Open record", "small", "patient-summary"),
      ],
    ];
    return shell(
      pageHeader(
        "Staff operations",
        "Patient registry",
        "Search before registering a new patient to reduce duplicate records.",
        button("Register patient", "primary", "register-patient"),
      ) +
        `<div class="toolbar">${field("Search patients", input("search", "", 'placeholder="Name, MRN, phone or email"'))}${field("Lifecycle", select(["All statuses", "Active", "Inactive", "Merged"], "All statuses"))}${button("Search", "primary", "notice")}${button("Check duplicates", "", "duplicate-check")}</div>${alertBox("Search uses exact patient context", "Opening a record replaces and clears the previous patient context.", "info")}<section class="card flush">${table("Patient search results", ["Patient", "Date of birth", "Phone", "Lifecycle", "Action"], rows)}</section>`,
    );
  }

  function patientSummary() {
    return shell(
      patientTabs("summary") +
        `<div class="tab-panel">${pageHeader("Clinical chart", "Patient summary", "Last refreshed 09 Sep 2026, 09:02 EAT.", button("Refresh", "", "notice") + button("Start encounter", "primary", "encounter-form"))}${alertBox("Confirmed allergy: penicillin", "Avoid penicillin-class medications. Allergy recorded 12 March 2024 by Dr. M. Abdalla.", "danger")}<div class="grid four">${metric("Open alerts", "2", "1 high priority", "warning")}${metric("Active medications", "4", "Reconciled 08 Sep")}${metric("Upcoming visits", "1", "Cardiology · 12 Sep")}${metric("Outstanding", "KES 4,850", "Invoice INV-2026-0812", "warning")}</div><div class="grid main-aside section-gap"><div class="stack">${card("Active care plan", `<dl class="definition-list"><div><dt>Primary concern</dt><dd>Hypertension follow-up</dd></div><div><dt>Last BP</dt><dd>142/88 mmHg · 08 Sep</dd></div><div><dt>Care team</dt><dd>Dr. E. Okoth · Nurse J. Achieng</dd></div><div><dt>Next review</dt><dd>12 Sep 2026 · 09:30 EAT</dd></div></dl>`)}${card("Recent clinical activity", `<ul class="timeline"><li><strong>Metabolic panel result received</strong><span>Today, 08:03 · CityLab integration</span></li><li><strong>Medication list reconciled</strong><span>08 Sep, 15:21 · Dr. E. Okoth</span></li><li><strong>Outpatient encounter closed</strong><span>08 Sep, 15:18 · Encounter ENC-4831</span></li></ul>`)}</div>${card("Safety and access", `<p>${status("Standard access active", "success")}</p><p>${status("1 unacknowledged alert", "danger")}</p><p>${status("Consent valid to 31 Dec", "info")}</p><hr style="border:0;border-top:1px solid var(--border);margin:1rem 0"><p class="muted">FHIR record access is available under the current consent. Emergency access is not active.</p>${button("Review safety alerts", "small", "route-medications")}`)}</div></div>`,
      true,
    );
  }

  function demographicsPage() {
    return shell(
      patientTabs("demographics") +
        `<div class="tab-panel">${pageHeader("Patient record", "Demographics", "Identity fields are shown for confirmation before clinical or billing actions.", button("Edit demographics", "primary", "edit-demographics") + button("Change lifecycle", "", "lifecycle"))}<div class="grid main-aside">${card("Identity and contact", `<dl class="definition-list"><div><dt>Legal name</dt><dd>Amara Njeri</dd></div><div><dt>MRN</dt><dd>MRN-1047-62</dd></div><div><dt>Date of birth</dt><dd>14 February 1988</dd></div><div><dt>Gender</dt><dd>Female</dd></div><div><dt>Email</dt><dd>a••••@example.test</dd></div><div><dt>Phone</dt><dd>+254 7•• ••• 901</dd></div><div><dt>Address</dt><dd>Redacted in mockup</dd></div><div><dt>Registered</dt><dd>12 March 2024</dd></div></dl>`)}<div class="stack">${card("Lifecycle", `<p>${status("Active", "success")}</p><p class="muted">Last changed 12 Mar 2024 by Registration Desk 2.</p>`)}${card("Potential duplicates", `<p><strong>1 possible match</strong></p><p class="muted">Review identity attributes side by side before considering a merge.</p>${button("Review match", "small", "duplicate-check")}`)}</div></div></div>`,
      true,
    );
  }

  function encountersPage() {
    const rows = [
      [
        "ENC-4831",
        "Outpatient review",
        "08 Sep 2026 · 14:45",
        "Dr. E. Okoth",
        status("Closed · signed", "success"),
        button("View", "small", "notice"),
      ],
      [
        "ENC-4620",
        "Emergency assessment",
        "19 Aug 2026 · 18:22",
        "Dr. R. Ndegwa",
        status("Closed · signed", "success"),
        button("View", "small", "notice"),
      ],
      [
        "ENC-4407",
        "Nurse triage",
        "10 Aug 2026 · 09:12",
        "Nurse J. Achieng",
        status("Closed", "neutral"),
        button("View", "small", "notice"),
      ],
    ];
    return shell(
      patientTabs("encounters") +
        `<div class="tab-panel">${pageHeader("Clinical chart", "Encounters and notes", "Signed notes are immutable and require an addendum workflow.", button("Start encounter", "primary", "encounter-form"))}<div class="card flush">${table("Patient encounters", ["Encounter", "Type", "Date & time (EAT)", "Responsible clinician", "Status", "Action"], rows)}</div><div class="grid two section-gap">${card("Selected note · ENC-4831", `<p><strong>Hypertension follow-up</strong> ${status("Signed", "success")}</p><p class="muted">Patient reports adherence to current therapy. Blood pressure remains above target. Continue monitoring and review after laboratory results.</p><div class="privacy-note">Signed 08 Sep 2026, 15:16 EAT by Dr. E. Okoth. This note cannot be edited.</div><div class="form-actions">${button("Add addendum", "small", "notice")}</div>`)}${card(
          "Encounter diagnoses",
          table(
            "Diagnoses for ENC-4831",
            ["Code", "Diagnosis", "Status"],
            [
              ["I10", "Essential hypertension", status("Active", "warning")],
              ["E78.5", "Hyperlipidaemia", status("Active", "warning")],
            ],
          ),
          "",
          "flush",
        )}</div></div>`,
      true,
    );
  }

  function medicationsPage() {
    const rows = [
      [
        "Amlodipine",
        "5 mg oral",
        "Once daily",
        "01 Jul 2026",
        "Dr. E. Okoth",
        status("Active", "success"),
        button("Review", "small", "notice"),
      ],
      [
        "Atorvastatin",
        "20 mg oral",
        "At night",
        "15 May 2026",
        "Dr. M. Abdalla",
        status("Active", "success"),
        button("Review", "small", "notice"),
      ],
      [
        "Amoxicillin",
        "500 mg oral",
        "Three times daily",
        "Blocked",
        "Dr. E. Okoth",
        status("Safety blocked", "danger"),
        button("View conflict", "small danger", "safety-conflict"),
      ],
    ];
    return shell(
      patientTabs("medications") +
        `<div class="tab-panel">${pageHeader("Clinical chart", "Medications", "Review allergies, safety alerts and current therapy before prescribing.", button("New prescription", "primary", "prescription-form"))}${alertBox("Medication safety block", "Amoxicillin conflicts with the confirmed penicillin allergy. The prescription was not created.", "danger")}<div class="grid main-aside"><section class="card flush">${table("Medication list", ["Medication", "Dose", "Frequency", "Started", "Prescriber", "Status", "Action"], rows)}</section>${card("Clinical alerts", `<p>${status("High priority · open", "danger")}</p><p><strong>Allergy interaction</strong></p><p class="muted">Source: prescription check · Assigned to Dr. E. Okoth</p>${button("Acknowledge alert", "small", "ack-alert")}<hr style="border:0;border-top:1px solid var(--border);margin:1rem 0"><p>${status("Reviewed", "success")}</p><p><strong>Duplicate therapy check</strong></p><p class="muted">Acknowledged 08 Sep, 15:20 EAT.</p>`)}</div></div>`,
      true,
    );
  }

  function labsPage() {
    const rows = [
      [
        "Potassium",
        "6.2 mmol/L",
        "3.5–5.1",
        status("Critical high", "danger"),
        "09 Sep 2026 · 08:03",
        "CityLab",
        button("Review", "small danger", "lab-review"),
      ],
      [
        "Creatinine",
        "78 µmol/L",
        "45–90",
        status("Within range", "success"),
        "09 Sep 2026 · 08:03",
        "CityLab",
        button("View trend", "small", "notice"),
      ],
      [
        "HbA1c",
        "5.8%",
        "4.0–5.6",
        status("High", "warning"),
        "22 Aug 2026 · 11:40",
        "Northstar Lab",
        button("View trend", "small", "notice"),
      ],
    ];
    return shell(
      patientTabs("labs") +
        `<div class="tab-panel">${pageHeader("Clinical chart", "Laboratory results", "Imported and internal results. Critical values require documented review.", button("Add result", "primary", "lab-form"))}${alertBox("Critical result requires review", "Potassium 6.2 mmol/L received at 08:03 EAT. Safety task is overdue and unassigned.", "danger")}<div class="card flush">${table("Laboratory results", ["Test", "Result", "Reference range", "Interpretation", "Collected", "Source", "Action"], rows)}</div></div>`,
      true,
    );
  }

  function patientStaffBilling() {
    const rows = [
      [
        "INV-2026-0812",
        "28 Aug 2026",
        "Cardiology consultation",
        "KES 6,850",
        "KES 2,000",
        "KES 4,850",
        status("Posted · part paid", "warning"),
        button("Open", "small", "notice"),
      ],
      [
        "INV-2026-0631",
        "16 Aug 2026",
        "Laboratory services",
        "KES 3,200",
        "KES 3,200",
        "KES 0",
        status("Paid", "success"),
        button("Open", "small", "notice"),
      ],
    ];
    return shell(
      patientTabs("billing") +
        `<div class="tab-panel">${pageHeader("Revenue cycle", "Patient billing", "Account BIL-1047 · Currency KES.", button("Create invoice", "primary", "invoice-form") + button("Post payment", "", "posting-form"))}<div class="grid three">${metric("Total invoiced", "KES 10,050", "2 posted invoices")}${metric("Total paid", "KES 5,200", "Latest 08 Sep")}${metric("Outstanding", "KES 4,850", "No overdue balance", "warning")}</div><div class="card flush section-gap">${table("Patient invoices", ["Invoice", "Issued", "Description", "Amount", "Paid", "Outstanding", "Status", "Action"], rows)}</div></div>`,
      true,
    );
  }

  function schedulePage() {
    return shell(
      pageHeader(
        "Staff operations",
        "Schedule",
        "Week of 7–11 September 2026 · East Africa Time (EAT).",
        button("Book appointment", "primary", "booking-form") +
          button("Add availability", "", "availability-form"),
      ) +
        `<div class="toolbar">${field("Clinician", select(["Dr. E. Okoth", "Dr. W. Muriithi", "All clinicians"], "Dr. E. Okoth"))}${field("Location", select(["Westlands Medical Centre", "All locations"], "Westlands Medical Centre"))}${field("View", select(["Week", "Day", "List"], "Week"))}${button("Today", "", "notice")}</div>
      <div class="calendar" role="grid" aria-label="Weekly appointment calendar"><div class="cal-cell cal-head">Time</div>${["Mon 7", "Tue 8", "Wed 9", "Thu 10", "Fri 11"].map((d) => `<div class="cal-cell cal-head">${d} Sep</div>`).join("")}${["08:00", "09:00", "10:00", "11:00", "12:00"].map((time, i) => `<div class="cal-cell cal-time">${time}</div>${[0, 1, 2, 3, 4].map((_, day) => `<div class="cal-cell">${(i + day) % 4 === 0 ? `<div class="cal-event">${i === 0 ? "Round" : "Review"}<br>${i === 0 ? "Ward B" : "30 min"}</div>` : i === 2 && day === 1 ? '<div class="cal-event warning">Slot held<br>Conflict review</div>' : ""}</div>`).join("")}`).join("")}</div>`,
    );
  }

  function insurancePage() {
    const rows = [
      [
        "CLM-02418",
        "INV-2026-0812",
        "AfyaCare Gold",
        "KES 12,600",
        "KES 8,000",
        status("Adjudicated", "info"),
        "08 Sep 2026",
        button("Open", "small", "claim-detail"),
      ],
      [
        "CLM-02344",
        "INV-2026-0631",
        "AfyaCare Gold",
        "KES 3,200",
        "KES 3,200",
        status("Paid", "success"),
        "18 Aug 2026",
        button("Open", "small", "notice"),
      ],
      [
        "CLM-02291",
        "INV-2026-0528",
        "AfyaCare Gold",
        "KES 7,450",
        "KES 0",
        status("Denied", "danger"),
        "02 Aug 2026",
        button("Review denial", "small", "notice"),
      ],
    ];
    return shell(
      pageHeader(
        "Revenue cycle",
        "Insurance",
        "Coverage, claims, adjudication and remittance reconciliation.",
        button("New claim", "primary", "claim-form") +
          button("Verify coverage", "", "coverage-form"),
      ) +
        `<div class="grid four">${metric("Submitted", "8", "KES 94,200")}${metric("Adjudicated", "5", "2 need reconciliation", "warning")}${metric("Paid", "21", "This month")}${metric("Denied", "2", "Requires follow-up", "warning")}</div><div class="toolbar section-gap">${field("Status", select(["All claims", "Submitted", "Adjudicated", "Paid", "Denied"], "All claims"))}${field("Payer", select(["All payers", "AfyaCare", "Jubilee Health"], "All payers"))}<div class="field grow">${field("Search", input("search", "", 'placeholder="Claim, invoice or policy"'))}</div>${button("Apply", "primary", "notice")}</div><div class="card flush">${table("Insurance claims", ["Claim", "Invoice", "Payer", "Claimed", "Approved", "Status", "Updated", "Action"], rows)}</div>`,
    );
  }

  function pharmacyPage() {
    const rows = [
      [
        status("Ready for review", "warning"),
        '<span class="row-title">RX-74018 · Amlodipine 5 mg</span><span class="row-subtitle">Daniel Otieno · MRN-1633-20</span>',
        "30 tablets",
        "Dr. E. Okoth",
        "Available",
        button("Safety review", "small primary", "safety-review"),
      ],
      [
        status("Safety blocked", "danger"),
        '<span class="row-title">RX-74011 · Amoxicillin 500 mg</span><span class="row-subtitle">Amara Njeri · MRN-1047-62</span>',
        "21 capsules",
        "Dr. E. Okoth",
        "Available",
        button("View conflict", "small danger", "safety-conflict"),
      ],
      [
        status("Approved", "success"),
        '<span class="row-title">RX-73998 · Atorvastatin 20 mg</span><span class="row-subtitle">Linet Wambui · MRN-1022-91</span>',
        "30 tablets",
        "Dr. M. Abdalla",
        "Available",
        button("Dispense", "small primary", "dispense"),
      ],
    ];
    return shell(
      pageHeader(
        "Pharmacy",
        "Prescription worklist",
        "Dispensing requires safety approval and sufficient unexpired stock.",
        button("Add prescription", "primary", "pharmacy-rx"),
      ) +
        alertBox(
          "1 prescription blocked",
          "A confirmed allergy conflict must be resolved by the prescribing clinician.",
          "danger",
        ) +
        `<div class="toolbar">${field("Review state", select(["Awaiting action", "All", "Approved", "Blocked"], "Awaiting action"))}${field("Location", select(["Westlands pharmacy", "All locations"], "Westlands pharmacy"))}<div class="field grow">${field("Search", input("search", "", 'placeholder="Prescription, patient or medication"'))}</div>${button("Apply", "primary", "notice")}</div><div class="card flush">${table("Prescription worklist", ["State", "Prescription", "Quantity", "Prescriber", "Stock", "Action"], rows)}</div>`,
    );
  }

  function inventoryPage() {
    const meds = [
      [
        "Amlodipine 5 mg",
        "MED-AML-005",
        "1,420",
        "420",
        "31 Dec 2027",
        status("In stock", "success"),
        button("Movements", "small", "notice"),
      ],
      [
        "Atorvastatin 20 mg",
        "MED-ATO-020",
        "310",
        "350",
        "30 Nov 2027",
        status("Below reorder", "warning"),
        button("Receive batch", "small primary", "batch-form"),
      ],
      [
        "Amoxicillin 500 mg",
        "MED-AMX-500",
        "84",
        "250",
        "10 Sep 2026",
        status("Expiring soon", "danger"),
        button("Review batches", "small", "notice"),
      ],
    ];
    return shell(
      pageHeader(
        "Pharmacy",
        "Inventory",
        "Medication and supply movements are recorded against reference IDs.",
        button("Receive stock", "primary", "batch-form") +
          button("Add item", "", "inventory-item"),
      ) +
        `<div class="grid four">${metric("Medication lines", "184", "Across 3 locations")}${metric("Below reorder", "11", "Action required", "warning")}${metric("Expiring in 30d", "7", "Quarantine review", "warning")}${metric("Stock movements", "326", "This week")}</div><div class="tabs section-gap"><button class="tab active">Medications</button><button class="tab">Supplies</button><button class="tab">Movement history</button></div><div class="card flush">${table("Medication inventory", ["Medication", "SKU", "Available", "Reorder level", "Nearest expiry", "State", "Action"], meds)}</div>`,
    );
  }

  function notificationsPage() {
    const rows = [
      [
        "NTF-9218",
        "Appointment reminder",
        "Patient · P-••1047",
        "SMS",
        status("Delivered", "success"),
        "09 Sep · 08:45",
        button("View", "small", "notice"),
      ],
      [
        "NTF-9217",
        "Bill alert",
        "Patient · P-••1633",
        "Email",
        status("Queued", "info"),
        "09 Sep · 08:42",
        button("View", "small", "notice"),
      ],
      [
        "NTF-9194",
        "Appointment reminder",
        "Patient · P-••1022",
        "SMS",
        status("Failed", "danger"),
        "08 Sep · 17:20",
        button("Retry review", "small", "notice"),
      ],
    ];
    return shell(
      pageHeader(
        "Operations",
        "Notifications",
        "Track queued and delivered operational messages without exposing message content.",
        button("New notification", "primary", "notification-form"),
      ) +
        `<div class="grid four">${metric("Queued", "14", "Oldest 4 minutes")}${metric("Delivered", "286", "Past 24 hours")}${metric("Failed", "3", "Manual review required", "warning")}${metric("Delivery rate", "98.9%", "Past 7 days")}</div><div class="toolbar section-gap">${field("Type", select(["All types", "Appointment reminder", "Bill alert", "MFA code"], "All types"))}${field("Delivery state", select(["All states", "Queued", "Delivered", "Failed"], "All states"))}<div class="field grow">${field("Recipient ID", input("search", "", 'placeholder="Opaque recipient ID"'))}</div>${button("Apply", "primary", "notice")}</div><div class="card flush">${table("Notification activity", ["ID", "Type", "Recipient", "Channel", "State", "Updated (EAT)", "Action"], rows)}</div>`,
    );
  }

  function compliancePage() {
    const rows = [
      [
        status("Overdue", "danger"),
        "CASE-2026-041",
        "Potential inappropriate record access",
        "Privacy incident",
        status("Investigating", "warning"),
        "07 Sep 2026",
        "M. Kirui",
        button("Open", "small primary", "case-detail"),
      ],
      [
        status("On hold", "warning"),
        "CASE-2026-037",
        "Records retention request",
        "Data request",
        status("Remediating", "info"),
        "12 Sep 2026",
        "M. Kirui",
        button("Open", "small", "case-detail"),
      ],
      [
        status("On track", "success"),
        "CASE-2026-043",
        "Consent scope review",
        "Privacy review",
        status("Open", "info"),
        "18 Sep 2026",
        "Unassigned",
        button("Open", "small", "case-detail"),
      ],
    ];
    return shell(
      pageHeader(
        "Privacy operations",
        "Compliance cases",
        "Manage evidence, legal holds and valid workflow transitions.",
        button("Create case", "primary", "case-form"),
      ) +
        alertBox(
          "2 emergency-access reviews pending",
          "The oldest grant expires from the review queue in 37 minutes. Reviewers must be independent.",
          "warning",
        ) +
        `<div class="tabs"><button class="tab active">Cases</button><button class="tab">Emergency access reviews · 2</button><button class="tab">Consent grants</button></div><div class="toolbar tab-panel">${field("Case state", select(["Open cases", "All cases", "Overdue", "On hold"], "Open cases"))}${field("Kind", select(["All kinds", "Privacy incident", "Data request", "Privacy review"], "All kinds"))}<div class="field grow">${field("Search", input("search", "", 'placeholder="Case ID or title"'))}</div>${button("Apply", "primary", "notice")}</div><div class="card flush">${table("Compliance case queue", ["Due", "Case", "Title", "Kind", "State", "Due date", "Owner", "Action"], rows)}</div>`,
    );
  }

  function auditPage() {
    const rows = [
      [
        '09 Sep 2026<br><span class="muted">08:58:14 EAT</span>',
        "user-••27",
        "READ",
        "Patient record",
        "pat-••1047",
        "Clinical care",
        status("Verified", "success"),
        "REQ-UAT-81BA",
      ],
      [
        '09 Sep 2026<br><span class="muted">08:45:02 EAT</span>',
        "system-notify",
        "CREATE",
        "Notification",
        "ntf-••9218",
        "Appointment reminder",
        status("Verified", "success"),
        "REQ-UAT-6A10",
      ],
      [
        '09 Sep 2026<br><span class="muted">08:15:33 EAT</span>',
        "user-••91",
        "BREAK_GLASS",
        "FHIR record",
        "pat-••1633",
        "Emergency treatment",
        status("Review pending", "warning"),
        "REQ-UAT-3CD2",
      ],
    ];
    return shell(
      pageHeader(
        "Governance",
        "Audit evidence",
        "Append-only access evidence. Filters use opaque identifiers only.",
        button("Export filtered evidence", "", "export-audit"),
      ) +
        alertBox(
          "Audit events are read-only",
          "Corrections are appended as new evidence; existing events cannot be changed or removed.",
          "info",
        ) +
        `<div class="toolbar">${field("Patient ID", input("search", "", 'placeholder="Opaque UUID"'))}${field("Actor ID", input("search", "", 'placeholder="Opaque UUID"'))}${field("Action", select(["All actions", "READ", "CREATE", "UPDATE", "BREAK_GLASS"], "All actions"))}${field("From", input("date", "2026-09-09"))}${button("Apply filters", "primary", "notice")}</div><div class="card flush">${table("Audit events", ["Occurred", "Actor", "Action", "Resource", "Subject", "Purpose", "Integrity", "Request ID"], rows)}</div>`,
    );
  }

  function reportsPage() {
    const trends = `<div class="bar-list">${[
      ["Hypertension follow-up completion", 84],
      ["Diabetes HbA1c monitoring", 71],
      ["Medication reconciliation", 92],
      ["Post-discharge contact", 63],
    ]
      .map(
        ([label, pct]) =>
          `<div><div class="bar-head"><strong>${label}</strong><span>${pct}%</span></div><div class="progress-track"><div class="progress-fill" style="width:${pct}%"></div></div></div>`,
      )
      .join("")}</div>`;
    const rows = [
      [
        "RPT-2026-Q3-01",
        "Quarterly service activity",
        "Regulatory",
        "01 Jul – 30 Sep",
        status("Draft", "warning"),
        "09 Sep 2026",
        button("Open", "small", "notice"),
      ],
      [
        "RPT-2026-08-12",
        "Chronic disease outcomes",
        "Clinical quality",
        "01–31 Aug",
        status("Ready", "success"),
        "05 Sep 2026",
        button("Download", "small", "notice"),
      ],
      [
        "RPT-2026-08-02",
        "Access governance",
        "Compliance",
        "01–31 Aug",
        status("Submitted", "info"),
        "03 Sep 2026",
        button("View receipt", "small", "notice"),
      ],
    ];
    return shell(
      pageHeader(
        "Analytics",
        "Reports",
        "Aggregate operational and population-health information only.",
        button("Create report", "primary", "report-form"),
      ) +
        `<div class="grid four">${metric("Active cohort", "1,842", "Hypertension")}${metric("Follow-up rate", "84%", "Target 90%", "warning")}${metric("Reports due", "2", "Within 14 days", "warning")}${metric("Data through", "08 Sep", "Nightly refresh")}</div><div class="grid main-aside section-gap">${card("Quality measures", trends, "Current reporting period")}${card("Cohort summary", `<dl class="definition-list"><div><dt>Cohort</dt><dd>Adults with hypertension</dd></div><div><dt>Patients</dt><dd>1,842</dd></div><div><dt>Measurement period</dt><dd>01 Jul – 30 Sep 2026</dd></div><div><dt>Facilities</dt><dd>4 included</dd></div></dl><div class="privacy-note" style="margin-top:1rem">Small cell counts are suppressed to reduce re-identification risk.</div>`)}</div><div class="card flush section-gap">${table("Regulatory and quality reports", ["Report", "Name", "Type", "Period", "State", "Updated", "Action"], rows)}</div>`,
    );
  }

  function renderRoute() {
    const path = route();
    if (path.startsWith("/patient/")) state.view = "patient";
    if (path.startsWith("/staff/")) state.view = "staff";
    const routes = {
      "/login": loginPage,
      "/forbidden": forbiddenPage,
      "/states": statesPage,
      "/patient/overview": patientOverview,
      "/patient/appointments": patientAppointments,
      "/patient/records": patientRecords,
      "/patient/billing": patientBilling,
      "/patient/access": patientAccess,
      "/staff/work-queue": workQueue,
      "/staff/patients": patientsPage,
      "/staff/patients/pat-1047/summary": patientSummary,
      "/staff/patients/pat-1047/demographics": demographicsPage,
      "/staff/patients/pat-1047/encounters": encountersPage,
      "/staff/patients/pat-1047/medications": medicationsPage,
      "/staff/patients/pat-1047/labs": labsPage,
      "/staff/patients/pat-1047/billing": patientStaffBilling,
      "/staff/schedule": schedulePage,
      "/staff/insurance": insurancePage,
      "/staff/pharmacy": pharmacyPage,
      "/staff/inventory": inventoryPage,
      "/staff/notifications": notificationsPage,
      "/staff/compliance": compliancePage,
      "/staff/audit": auditPage,
      "/staff/reports": reportsPage,
    };
    const renderer = routes[path] || workQueue;
    app.innerHTML = renderer();
    bindEvents();
    requestAnimationFrame(() => document.querySelector("h1")?.focus());
  }

  function bindEvents() {
    document.querySelectorAll("[data-route]").forEach((el) =>
      el.addEventListener("click", () => {
        state.navOpen = false;
        state.indexOpen = false;
        location.hash = el.dataset.route;
      }),
    );
    document.querySelectorAll("[data-view]").forEach((el) =>
      el.addEventListener("click", () => {
        state.view = el.dataset.view;
        location.hash =
          state.view === "patient"
            ? "#/patient/overview"
            : "#/staff/work-queue";
      }),
    );
    document.querySelector("[data-role]")?.addEventListener("change", (e) => {
      state.role = e.target.value;
      renderRoute();
    });
    document
      .querySelectorAll("[data-action]")
      .forEach((el) =>
        el.addEventListener("click", (event) =>
          handleAction(event.currentTarget.dataset.action, event.currentTarget),
        ),
      );
    document.querySelectorAll("form[data-submit]").forEach((form) =>
      form.addEventListener("submit", (e) => {
        e.preventDefault();
        state.view =
          form.querySelector("select")?.value === "Patient portal"
            ? "patient"
            : "staff";
        location.hash =
          state.view === "patient"
            ? "#/patient/overview"
            : "#/staff/work-queue";
      }),
    );
  }

  function handleAction(action, source) {
    if (action === "submit") {
      source.closest("form")?.requestSubmit();
      return;
    }
    const routeActions = {
      home:
        state.view === "patient" ? "#/patient/overview" : "#/staff/work-queue",
      logout: "#/login",
      submit: null,
      "route-appointments": "#/patient/appointments",
      "route-access": "#/patient/access",
      "patient-summary": "#/staff/patients/pat-1047/summary",
      "route-medications": "#/staff/patients/pat-1047/medications",
    };
    if (routeActions[action]) {
      location.hash = routeActions[action];
      return;
    }
    if (action === "toggle-nav") {
      state.navOpen = !state.navOpen;
      renderRoute();
      return;
    }
    if (action === "open-index") {
      state.indexOpen = !state.indexOpen;
      renderRoute();
      return;
    }
    if (action === "change-patient") {
      showDialog(
        "Change patient context",
        "Opening another record clears cached information for Amara Njeri before the next patient is shown.",
        `${field("Find patient", input("search", "", 'placeholder="Name or MRN" autofocus'))}${alertBox("Context safety", "Confirm identity and date of birth before opening a different record.", "warning")}`,
        "Open selected patient",
      );
      return;
    }
    const dialogs = {
      "appointment-form": [
        "Request an appointment",
        "Tell us the care you need. A request is not confirmed until scheduling resolves it.",
        appointmentForm(),
        "Submit request",
      ],
      "cancel-appointment": [
        "Cancel appointment request",
        "Cancel the unresolved annual wellness visit request submitted 08 September 2026?",
        `${field('Reason <span class="required">*</span>', `<textarea required>Plans changed</textarea>`)}${alertBox("This cannot be undone", "The request will leave the scheduling queue. You may submit a new request later.", "warning")}`,
        "Cancel request",
        true,
      ],
      "record-form": [
        "Request health records",
        "Choose only the information and date range you need.",
        recordForm(),
        "Submit request",
      ],
      "download-record": [
        "Download health records",
        "Laboratory results for the period 01–16 August 2026 are ready.",
        alertBox(
          "Private download",
          "The text file may contain sensitive health information. Save it only on a trusted device.",
          "warning",
        ),
        "Download records.txt",
      ],
      "payment-form": [
        "Make a payment",
        "Payment is applied to invoice INV-2026-0812 for Samira Kamau.",
        paymentForm(),
        "Submit payment",
      ],
      "proxy-form": [
        "Add proxy access",
        "Grant one narrow access scope to a trusted person.",
        proxyForm(),
        "Create grant",
      ],
      "revoke-proxy": [
        "Revoke proxy access",
        "Remove Amina Kamau’s Records access for Samira Kamau?",
        alertBox(
          "Access ends immediately",
          "Amina will no longer be able to view or request records.",
          "warning",
        ),
        "Revoke access",
        true,
      ],
      "task-form": [
        "Create staff task",
        "Create work in a permitted queue without adding patient details to the title.",
        taskForm(),
        "Create task",
      ],
      "claim-task": [
        "Claim urgent task",
        "Assign “Review critical potassium result” for Amara Njeri to Dr. Elias Okoth?",
        alertBox(
          "Clinical responsibility",
          "The task remains overdue until it is completed.",
          "warning",
        ),
        "Claim task",
      ],
      "register-patient": [
        "Register patient",
        "Search for duplicates before creating a patient identity.",
        patientRegistrationForm(),
        "Review registration",
      ],
      "duplicate-check": [
        "Potential duplicate review",
        "Compare identity attributes before any merge decision.",
        duplicateDialog(),
        "Continue to merge review",
      ],
      "encounter-form": [
        "Start encounter",
        "Open a clinical encounter for Amara Njeri (MRN-1047-62).",
        encounterForm(),
        "Start encounter",
      ],
      "edit-demographics": [
        "Edit demographics",
        "Changes apply to Amara Njeri after server validation.",
        demographicsForm(),
        "Save changes",
      ],
      lifecycle: [
        "Change patient lifecycle",
        "Select a valid lifecycle transition for Amara Njeri.",
        `${field("New status", select(["Inactive", "Deceased"]))}${field('Reason <span class="required">*</span>', `<textarea required></textarea>`)}${alertBox("Review required", "Lifecycle changes affect record access and downstream workflows.", "warning")}`,
        "Confirm status change",
        true,
      ],
      "prescription-form": [
        "New prescription",
        "Safety checks run before the prescription is created for Amara Njeri.",
        prescriptionForm(),
        "Run safety check",
      ],
      "safety-conflict": [
        "Medication safety conflict",
        "Amoxicillin was blocked for Amara Njeri.",
        alertBox(
          "Confirmed penicillin allergy",
          "Do not proceed. Select a safe alternative or discontinue the draft prescription.",
          "danger",
        ),
        "Return to medication list",
      ],
      "ack-alert": [
        "Acknowledge clinical alert",
        "Confirm that you reviewed the penicillin allergy interaction for Amara Njeri.",
        `${field('Clinical note <span class="required">*</span>', `<textarea required placeholder="Document your review"></textarea>`)}${alertBox("Acknowledgement is audited", "Acknowledging does not remove the allergy or permit unsafe dispensing.", "warning")}`,
        "Acknowledge alert",
      ],
      "lab-review": [
        "Review critical laboratory result",
        "Potassium 6.2 mmol/L for Amara Njeri, collected 09 Sep 2026 at 08:03 EAT.",
        `${field('Review outcome <span class="required">*</span>', select(["Contact patient now", "Escalate to care team", "Result invalid — request repeat"]))}${field('Clinical note <span class="required">*</span>', `<textarea required></textarea>`)}`,
        "Record review",
      ],
      "invoice-form": [
        "Create invoice",
        "Create a billing item for Amara Njeri in KES.",
        invoiceForm(),
        "Create invoice",
      ],
      "posting-form": [
        "Post payment",
        "Apply a payment to INV-2026-0812. Outstanding balance: KES 4,850.",
        paymentPostingForm(),
        "Post payment",
      ],
      "booking-form": [
        "Book appointment",
        "Confirm patient, clinician, time and duration before booking.",
        bookingForm(),
        "Book appointment",
      ],
      "availability-form": [
        "Add clinician availability",
        "Create an available schedule block in East Africa Time.",
        availabilityForm(),
        "Add availability",
      ],
      "claim-form": [
        "Create insurance claim",
        "Submit one invoice against the verified policy.",
        claimForm(),
        "Submit claim",
      ],
      "coverage-form": [
        "Verify coverage",
        "Request current coverage evidence from the selected payer.",
        coverageForm(),
        "Request verification",
      ],
      "claim-detail": [
        "Claim CLM-02418",
        "Adjudicated for KES 8,000. Remittance has not been reconciled.",
        claimDetail(),
        "Record remittance",
      ],
      "safety-review": [
        "Prescription safety review",
        "Review RX-74018 before approval or dispensing.",
        safetyReviewForm(),
        "Approve safety review",
      ],
      dispense: [
        "Confirm dispensing",
        "Dispense RX-73998 for Linet Wambui?",
        dispenseForm(),
        "Confirm dispense",
      ],
      "pharmacy-rx": [
        "Add pharmacy prescription",
        "Create a pharmacy work item from a verified clinical prescription.",
        pharmacyPrescriptionForm(),
        "Add to worklist",
      ],
      "batch-form": [
        "Receive stock batch",
        "Record an idempotent stock receipt using the supplier reference.",
        batchForm(),
        "Receive batch",
      ],
      "inventory-item": [
        "Add inventory item",
        "Create a medication catalogue item with zero starting quantity.",
        inventoryItemForm(),
        "Create item",
      ],
      "notification-form": [
        "Create notification",
        "Send an operational notification without including unnecessary clinical detail.",
        notificationForm(),
        "Queue notification",
      ],
      "case-form": [
        "Create compliance case",
        "Evidence references must be identifiers, not patient details or URLs.",
        caseForm(),
        "Create case",
      ],
      "case-detail": [
        "Compliance case CASE-2026-041",
        "Potential inappropriate record access · Current state: Investigating.",
        caseDetail(),
        "Move to contained",
      ],
      "export-audit": [
        "Export audit evidence",
        "Export the currently filtered evidence set.",
        alertBox(
          "Sensitive evidence",
          "The export contains security metadata. Store and share it only through approved channels.",
          "warning",
        ),
        "Export CSV",
      ],
      "report-form": [
        "Create regulatory report",
        "Define the reporting period and intended report type.",
        reportForm(),
        "Create draft",
      ],
    };
    if (dialogs[action]) {
      showDialog(...dialogs[action]);
      return;
    }
    if (action === "notice")
      showToast("Prototype action completed. No data was changed.");
  }

  function showDialog(title, description, body, confirmText, danger = false) {
    previousFocus = document.activeElement;
    dialogRoot.innerHTML = `<div class="dialog-backdrop" role="presentation"><section class="dialog" role="dialog" aria-modal="true" aria-labelledby="dialog-title" aria-describedby="dialog-description"><header class="dialog-header"><h2 id="dialog-title">${title}</h2><p id="dialog-description">${description}</p></header><form data-dialog-form><div class="dialog-body">${body}</div><div class="dialog-actions">${button("Cancel", "", "close-dialog")}${button(confirmText, danger ? "danger" : "primary", "confirm-dialog")}</div></form></section></div>`;
    const first = dialogRoot.querySelector("input, select, textarea, button");
    first?.focus();
    dialogRoot
      .querySelector('[data-action="close-dialog"]')
      .addEventListener("click", (e) => {
        e.preventDefault();
        closeDialog();
        sourceFocus();
      });
    dialogRoot
      .querySelector('[data-action="confirm-dialog"]')
      .addEventListener("click", (e) => {
        e.preventDefault();
        e.currentTarget.closest("form").requestSubmit();
      });
    dialogRoot
      .querySelector("[data-dialog-form]")
      .addEventListener("submit", (e) => {
        e.preventDefault();
        closeDialog();
        showToast(`${confirmText} — mockup only. No data was changed.`);
        sourceFocus();
      });
    dialogRoot
      .querySelector(".dialog-backdrop")
      .addEventListener("click", (e) => {
        if (e.target === e.currentTarget) closeDialog();
      });
    dialogRoot.addEventListener("keydown", trapDialog);
  }

  let previousFocus;
  function sourceFocus() {
    previousFocus?.focus();
  }
  function closeDialog() {
    dialogRoot.innerHTML = "";
  }
  function trapDialog(e) {
    if (e.key === "Escape") {
      closeDialog();
      sourceFocus();
    }
    if (e.key !== "Tab") return;
    const els = [
      ...dialogRoot.querySelectorAll("button,input,select,textarea,[href]"),
    ].filter((x) => !x.disabled);
    if (!els.length) return;
    const first = els[0],
      last = els[els.length - 1];
    if (e.shiftKey && document.activeElement === first) {
      e.preventDefault();
      last.focus();
    }
    if (!e.shiftKey && document.activeElement === last) {
      e.preventDefault();
      first.focus();
    }
  }

  function showToast(message) {
    toastRoot.innerHTML = `<div class="toast">✓ ${message}</div>`;
    window.setTimeout(() => {
      toastRoot.innerHTML = "";
    }, 3200);
  }

  function appointmentForm() {
    return `<div class="form-grid">${field('Type <span class="required">*</span>', select(["Annual wellness visit", "Follow-up", "New concern"]))}${field('Preferred location <span class="required">*</span>', select(["Westlands Medical Centre", "Parklands Clinic"]))}${field('Preferred date <span class="required">*</span>', input("date", "2026-09-24", 'min="2026-09-09"'))}${field("Time preference", select(["Morning", "Afternoon", "Any time"]))}</div>${field('Reason for visit <span class="required">*</span>', `<textarea required>Annual wellness visit</textarea>`)}<p class="privacy-note">This submits a request. Scheduling will confirm the final clinician, time and location.</p>`;
  }
  function recordForm() {
    return `${field('Record type <span class="required">*</span>', select(["Clinical summary", "Laboratory results", "Visit summaries"]))}<div class="form-grid">${field("From date", input("date", "2026-01-01"))}${field("To date", input("date", "2026-09-09"))}</div>${field("Purpose (optional)", `<textarea placeholder="For example: personal copy"></textarea>`)}<label class="check-row"><input type="checkbox" required /><span>I understand the released file may contain private health information.</span></label>`;
  }
  function paymentForm() {
    return `<div class="form-grid">${field("Invoice", select(["INV-2026-0812 · KES 4,850"]))}${field('Amount (KES) <span class="required">*</span>', input("text", "4850.00", 'inputmode="decimal"'))}</div>${field('Payment method <span class="required">*</span>', select(["Mobile payment", "Card", "Bank transfer"]))}${alertBox("Review before submitting", "A unique payment reference protects this submission from accidental duplication.", "info")}`;
  }
  function proxyForm() {
    return `${field('Proxy person <span class="required">*</span>', select(["Select verified portal identity", "Amina Kamau"]))}<div class="form-grid">${field('Relationship <span class="required">*</span>', select(["Parent", "Spouse", "Guardian", "Other"]))}${field('Access scope <span class="required">*</span>', select(["RECORDS", "APPOINTMENTS", "PAYMENTS"]))}${field("Starts", input("date", "2026-09-09"))}${field('Expires <span class="required">*</span>', input("date", "2026-11-30"))}</div>${alertBox("One scope per grant", "Create separate grants if the proxy needs more than one kind of access.", "info")}`;
  }
  function taskForm() {
    return `<div class="form-grid">${field('Queue <span class="required">*</span>', select(["Clinical", "Nursing", "Operations"]))}${field('Priority <span class="required">*</span>', select(["Routine", "Urgent"]))}${field('Due at <span class="required">*</span>', input("datetime-local", "2026-09-09T14:00"))}${field("Patient", select(["No patient context", "Amara Njeri · MRN-1047-62"]))}</div>${field('Task title <span class="required">*</span>', input("text", "", 'placeholder="Action-oriented title"'))}${field("Instructions", `<textarea></textarea>`)}<p class="privacy-note">Keep the title free of diagnoses and unnecessary clinical details.</p>`;
  }
  function patientRegistrationForm() {
    return `${alertBox("Duplicate search completed", "No close matches were found for the details entered.", "success")}<div class="form-grid">${field('Given name <span class="required">*</span>', input("text", "Zuri"))}${field('Family name <span class="required">*</span>', input("text", "Kilonzo"))}${field('Date of birth <span class="required">*</span>', input("date", "1996-04-18"))}${field('Gender <span class="required">*</span>', select(["Female", "Male", "Other", "Unknown"]))}${field("Phone", input("tel", "+254 700 000 416"))}${field("Email", input("email", "zuri.kilonzo@example.test"))}</div>${field("Address", `<textarea>12 Example Road, Nairobi</textarea>`)}`;
  }
  function duplicateDialog() {
    return `<div class="grid two">${card("Source record", `<p><strong>Amara Njeri</strong></p><p class="muted">MRN-1047-62 · 14 Feb 1988<br>+254 7•• ••• 901</p>${status("Active", "success")}`)}${card("Possible match", `<p><strong>Amara N. Kariuki</strong></p><p class="muted">MRN-2091-31 · 14 Feb 1988<br>+254 7•• ••• 901</p>${status("Inactive", "neutral")}`)}</div>${alertBox("Merge is not yet authorized", "A later review must identify the survivor, show downstream effects and require deliberate confirmation.", "warning")}`;
  }
  function encounterForm() {
    return `<div class="form-grid">${field('Encounter type <span class="required">*</span>', select(["Outpatient review", "Inpatient review", "Telemedicine"]))}${field('Started at (EAT) <span class="required">*</span>', input("datetime-local", "2026-09-09T09:15"))}</div>${field('Reason <span class="required">*</span>', input("text", "Hypertension follow-up"))}${field("Initial note", `<textarea></textarea>`)}<p class="privacy-note">The encounter remains open until explicitly closed. Notes require a separate signing action.</p>`;
  }
  function demographicsForm() {
    return `<div class="form-grid">${field('Given name <span class="required">*</span>', input("text", "Amara"))}${field('Family name <span class="required">*</span>', input("text", "Njeri"))}${field('Date of birth <span class="required">*</span>', input("date", "1988-02-14"))}${field('Gender <span class="required">*</span>', select(["Female", "Male", "Other", "Unknown"], "Female"))}${field("Phone", input("tel", "+254 700 000 901"))}${field("Email", input("email", "amara.njeri@example.test"))}</div>${field("Address", `<textarea>Redacted synthetic address</textarea>`)}`;
  }
  function prescriptionForm() {
    return `<div class="form-grid">${field('Medication <span class="required">*</span>', select(["Select medication", "Amlodipine 5 mg", "Amoxicillin 500 mg"]))}${field('Route <span class="required">*</span>', select(["Oral", "Topical", "Intravenous"]))}${field('Dose <span class="required">*</span>', input("text", "5 mg"))}${field('Frequency <span class="required">*</span>', input("text", "Once daily"))}${field("Duration", input("text", "30 days"))}${field('Quantity <span class="required">*</span>', input("number", "30", 'min="1"'))}</div>${field("Instructions", `<textarea>Take with water.</textarea>`)}${alertBox("Safety checks required", "Allergy, interaction and duplicate therapy checks run before creation. A conflict will block this action.", "warning")}`;
  }
  function labForm() {
    return `<div class="form-grid">${field('Test <span class="required">*</span>', input("text", ""))}${field('Collected at (EAT) <span class="required">*</span>', input("datetime-local", "2026-09-09T09:00"))}${field('Result <span class="required">*</span>', input("text", ""))}${field('Unit <span class="required">*</span>', input("text", ""))}${field("Reference range", input("text", ""))}${field("Interpretation", select(["Within range", "High", "Low", "Critical"]))}</div>`;
  }
  function invoiceForm() {
    return `<div class="form-grid">${field('Description <span class="required">*</span>', input("text", ""))}${field('Amount (KES) <span class="required">*</span>', input("text", "", 'inputmode="decimal"'))}${field('Reference <span class="required">*</span>', input("text", "INV-UAT-", 'class="mono"'))}${field('Service date <span class="required">*</span>', input("date", "2026-09-09"))}</div>${alertBox("Reference must be unique", "Reusing a reference with different details will be rejected as a conflict.", "info")}`;
  }
  function paymentPostingForm() {
    return `<div class="form-grid">${field('Amount (KES) <span class="required">*</span>', input("text", "4850.00", 'inputmode="decimal"'))}${field('Payment date <span class="required">*</span>', input("date", "2026-09-09"))}</div>${field('Payment reference <span class="required">*</span>', input("text", "PAY-UAT-", 'class="mono"'))}${alertBox("Balance validation", "Overpayments and mismatched reused references are rejected.", "warning")}`;
  }
  function bookingForm() {
    return `<div class="form-grid">${field('Patient <span class="required">*</span>', select(["Amara Njeri · MRN-1047-62", "Search patient"]))}${field('Clinician <span class="required">*</span>', select(["Dr. E. Okoth", "Dr. W. Muriithi"]))}${field('Starts (EAT) <span class="required">*</span>', input("datetime-local", "2026-09-12T09:30"))}${field('Duration (minutes) <span class="required">*</span>', input("number", "30", 'min="5" max="480"'))}${field('Location <span class="required">*</span>', select(["Westlands · Clinic 3", "Parklands · Clinic 1"]))}${field("Type", select(["Outpatient", "Telemedicine"]))}</div>${alertBox("Slot validation", "The server confirms schedule availability. A concurrent booking returns a conflict for review.", "info")}`;
  }
  function availabilityForm() {
    return `<div class="form-grid">${field('Clinician <span class="required">*</span>', select(["Dr. E. Okoth", "Dr. W. Muriithi"]))}${field('Location <span class="required">*</span>', select(["Westlands Medical Centre", "Parklands Clinic"]))}${field('Starts (EAT) <span class="required">*</span>', input("datetime-local", "2026-09-14T08:00"))}${field('Ends (EAT) <span class="required">*</span>', input("datetime-local", "2026-09-14T16:00"))}</div>`;
  }
  function claimForm() {
    return `<div class="form-grid">${field('Invoice <span class="required">*</span>', select(["INV-2026-0812 · KES 6,850"]))}${field('Policy <span class="required">*</span>', select(["AfyaCare Gold · POL-••841"]))}${field('Claimed amount (KES) <span class="required">*</span>', input("text", "6850.00", 'inputmode="decimal"'))}${field("Service date", input("date", "2026-08-28"))}</div>${field('Claim details <span class="required">*</span>', `<textarea required></textarea>`)}${alertBox("Submission is not acceptance", "The claim remains Submitted until the payer adjudication is recorded.", "info")}`;
  }
  function coverageForm() {
    return `<div class="form-grid">${field('Patient <span class="required">*</span>', select(["Amara Njeri · MRN-1047-62"]))}${field('Policy <span class="required">*</span>', select(["AfyaCare Gold · POL-••841"]))}${field("Service category", select(["Outpatient", "Inpatient", "Pharmacy", "Laboratory"]))}${field("Service date", input("date", "2026-09-12"))}</div>`;
  }
  function claimDetail() {
    return `<dl class="definition-list"><div><dt>Claimed</dt><dd>KES 12,600</dd></div><div><dt>Approved</dt><dd>KES 8,000</dd></div><div><dt>State</dt><dd>${status("Adjudicated", "info")}</dd></div><div><dt>Invoice</dt><dd>INV-2026-0812</dd></div></dl><hr style="border:0;border-top:1px solid var(--border);margin:1rem 0"><div class="form-grid">${field("Remittance amount (KES)", input("text", "8000.00", "readonly"))}${field("Remittance reference", input("text", "REM-UAT-", 'class="mono"'))}</div>${alertBox("Exact amount required", "The remittance must match the approved amount before reconciliation.", "warning")}`;
  }
  function safetyReviewForm() {
    return `<p><strong>Amlodipine 5 mg · 30 tablets</strong></p><p class="muted">Patient: Daniel Otieno · Prescription RX-74018</p><label class="check-row"><input type="checkbox" required><span>Allergy check completed — no conflict found</span></label><label class="check-row"><input type="checkbox" required><span>Drug interaction check completed — no conflict found</span></label><label class="check-row"><input type="checkbox" required><span>Duplicate therapy check completed — no conflict found</span></label>${field("Reviewer note", `<textarea></textarea>`)}${alertBox("All checks are required", "Approval is recorded only when every safety check passes.", "warning")}`;
  }
  function dispenseForm() {
    return `<dl class="definition-list"><div><dt>Patient</dt><dd>Linet Wambui · MRN-1022-91</dd></div><div><dt>Prescription</dt><dd>RX-73998</dd></div><div><dt>Medication</dt><dd>Atorvastatin 20 mg</dd></div><div><dt>Quantity</dt><dd>30 tablets</dd></div><div><dt>Safety review</dt><dd>${status("Approved", "success")}</dd></div><div><dt>Stock</dt><dd>${status("Available · unexpired", "success")}</dd></div></dl><label class="check-row"><input type="checkbox" required><span>I verified the patient, medication, dose, quantity and counselling.</span></label>`;
  }
  function pharmacyPrescriptionForm() {
    return `<div class="form-grid">${field('Clinical prescription ID <span class="required">*</span>', input("text", "RX-", 'class="mono"'))}${field('Medication <span class="required">*</span>', select(["Select medication", "Amlodipine 5 mg", "Atorvastatin 20 mg"]))}${field('Quantity <span class="required">*</span>', input("number", "30", 'min="1"'))}${field("Location", select(["Westlands pharmacy", "Parklands pharmacy"]))}</div>`;
  }
  function batchForm() {
    return `<div class="form-grid">${field('Medication <span class="required">*</span>', select(["Atorvastatin 20 mg", "Amlodipine 5 mg"]))}${field('Quantity <span class="required">*</span>', input("number", "500", 'min="1"'))}${field('Batch number <span class="required">*</span>', input("text", "BAT-UAT-"))}${field('Expiry <span class="required">*</span>', input("date", "2027-11-30", 'min="2026-09-10"'))}${field('Supplier reference <span class="required">*</span>', input("text", "SUP-UAT-", 'class="mono"'))}${field("Received at (EAT)", input("datetime-local", "2026-09-09T09:30"))}</div>${alertBox("Idempotent receipt", "Use the same supplier reference only when retrying this exact batch receipt.", "info")}`;
  }
  function inventoryItemForm() {
    return `<div class="form-grid">${field('Generic name <span class="required">*</span>', input("text", ""))}${field('Strength <span class="required">*</span>', input("text", ""))}${field('Form <span class="required">*</span>', select(["Tablet", "Capsule", "Liquid", "Injection"]))}${field('SKU <span class="required">*</span>', input("text", "MED-"))}${field('Reorder level <span class="required">*</span>', input("number", "100", 'min="1"'))}${field("Starting quantity", input("number", "0", "readonly"))}</div>`;
  }
  function notificationForm() {
    return `<div class="form-grid">${field('Type <span class="required">*</span>', select(["Appointment reminder", "Bill alert"]))}${field('Recipient <span class="required">*</span>', input("text", "", 'placeholder="Select permitted recipient"'))}${field('Channel <span class="required">*</span>', select(["SMS", "Email"]))}${field("Send at (EAT)", input("datetime-local", "2026-09-09T10:00"))}</div>${field('Message <span class="required">*</span>', `<textarea required>Your appointment is scheduled for 12 Sep at 09:30 EAT.</textarea>`)}${alertBox("Minimum necessary detail", "Do not include diagnoses, medication names, lab values or other clinical details in operational messages.", "warning")}`;
  }
  function caseForm() {
    return `<div class="form-grid">${field('Case kind <span class="required">*</span>', select(["Privacy incident", "Data request", "Privacy review"]))}${field('Due date <span class="required">*</span>', input("date", "2026-09-16"))}</div>${field('Title <span class="required">*</span>', input("text", ""))}${field("Evidence reference", input("text", "", 'placeholder="Opaque audit or document identifier"'))}${field('Initial assessment <span class="required">*</span>', `<textarea required></textarea>`)}<p class="privacy-note">Do not paste patient details, credentials, record contents or external URLs into evidence references.</p>`;
  }
  function caseDetail() {
    return `${alertBox("Valid next transition", "Investigating may move to Contained. Current server state will be checked before the transition.", "info")}<dl class="definition-list"><div><dt>Owner</dt><dd>M. Kirui</dd></div><div><dt>Due</dt><dd class="text-danger">07 Sep 2026 · overdue</dd></div><div><dt>Legal hold</dt><dd>None</dd></div><div><dt>Evidence</dt><dd class="mono">AUD-UAT-20841</dd></div></dl>${field('Transition note <span class="required">*</span>', `<textarea required></textarea>`)}<label class="check-row"><input type="checkbox" required><span>I verified the evidence and understand this transition is audited.</span></label>`;
  }
  function reportForm() {
    return `<div class="form-grid">${field('Report type <span class="required">*</span>', select(["Regulatory report", "Health trend", "Chronic disease cohort"]))}${field('Reporting facility <span class="required">*</span>', select(["All authorized facilities", "Westlands Medical Centre"]))}${field('Period starts <span class="required">*</span>', input("date", "2026-07-01"))}${field('Period ends <span class="required">*</span>', input("date", "2026-09-30"))}</div>${field('Report name <span class="required">*</span>', input("text", "Quarterly service activity"))}<p class="privacy-note">Reports use aggregate data. Small cell counts are suppressed.</p>`;
  }

  window.addEventListener("hashchange", renderRoute);
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && state.navOpen) {
      state.navOpen = false;
      renderRoute();
    }
  });
  renderRoute();
})();
