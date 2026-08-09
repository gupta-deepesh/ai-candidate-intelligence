const resumeInput =
    document.getElementById("resume");

const dropZone =
    document.getElementById("dropZone");

const jobDescription =
    document.getElementById("jobDescription");

const analyzeButton =
    document.getElementById("analyzeButton");

const resetButton =
    document.getElementById("resetButton");

const tryDemoButton =
    document.getElementById("tryDemoButton");

const demoAnalysisButton =
    document.getElementById("demoAnalysisButton");

const startAnalysisButton =
    document.getElementById("startAnalysisButton");

const analyzeAnotherButton =
    document.getElementById("analyzeAnotherButton");

const downloadJsonButton =
    document.getElementById("downloadJsonButton");

const loadingOverlay =
    document.getElementById("loadingOverlay");

const message =
    document.getElementById("message");

const results =
    document.getElementById("results");

const fileName =
    document.getElementById("fileName");

const elapsedTimer =
    document.getElementById("elapsedTimer");

const analysisDurationBadge =
    document.getElementById("analysisDurationBadge");

const loadingStatusText =
    document.getElementById("loadingStatusText");

const analysisDateBadge =
    document.getElementById("analysisDateBadge");

const loadingSpinner =
    document.getElementById("loadingSpinner");

const analysisCompleteIcon =
    document.getElementById("analysisCompleteIcon");

const loadingTitle =
    document.getElementById("loadingTitle");

const loadingDescription =
    document.getElementById("loadingDescription");

const typicalAnalysisTime =
    document.getElementById("typicalAnalysisTime");

const loadingCard =
    document.querySelector(".loading-card");


let lastAnalysis = null;

let loadingStepTimer = null;

let elapsedTimerHandle = null;

let analysisStartedAt = null;

let lastAnalysisDurationMs = 0;


/* ========================================================= */
/* HEALTH */
/* ========================================================= */

fetch("/api/health")
    .then(response =>
        response.json()
    )
    .then(data => {

        const badge =
            document.getElementById(
                "modeBadge"
            );

        badge.textContent =
            data.mockMode
                ? "MOCK MODE"
                : "CLAUDE LIVE";
    })
    .catch(() => {

        document.getElementById(
            "modeBadge"
        ).textContent =
            "Backend unavailable";
    });


/* ========================================================= */
/* HERO BUTTONS */
/* ========================================================= */

startAnalysisButton.addEventListener(
    "click",
    () => {

        document
            .getElementById("analyzer")
            .scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
    }
);


tryDemoButton.addEventListener(
    "click",
    runDemo
);


demoAnalysisButton.addEventListener(
    "click",
    runDemo
);


/* ========================================================= */
/* FILE */
/* ========================================================= */

resumeInput.addEventListener(
    "change",
    () => {

        const file =
            resumeInput.files[0];

        fileName.textContent =
            file
                ? file.name
                : "Maximum 10 MB";
    }
);


/* ========================================================= */
/* DRAG DROP */
/* ========================================================= */

[
    "dragenter",
    "dragover"
].forEach(eventName => {

    dropZone.addEventListener(
        eventName,
        event => {

            event.preventDefault();

            dropZone.classList.add(
                "dragover"
            );
        }
    );
});


[
    "dragleave",
    "drop"
].forEach(eventName => {

    dropZone.addEventListener(
        eventName,
        event => {

            event.preventDefault();

            dropZone.classList.remove(
                "dragover"
            );
        }
    );
});


dropZone.addEventListener(
    "drop",
    event => {

        const files =
            event.dataTransfer.files;

        if (!files.length) {
            return;
        }

        const selected =
            files[0];

        const transfer =
            new DataTransfer();

        transfer.items.add(
            selected
        );

        resumeInput.files =
            transfer.files;

        fileName.textContent =
            selected.name;
    }
);


/* ========================================================= */
/* LIVE ANALYSIS */
/* ========================================================= */

analyzeButton.addEventListener(
    "click",
    analyzeResume
);


async function analyzeResume() {

    clearMessage();

    const file =
        resumeInput.files[0];

    const jd =
        jobDescription.value.trim();


    if (!file) {

        showMessage(
            "Please upload a resume PDF."
        );

        return;
    }


    if (
        !file.name
            .toLowerCase()
            .endsWith(".pdf")
    ) {

        showMessage(
            "Only PDF resumes are supported."
        );

        return;
    }


    if (!jd) {

        showMessage(
            "Please paste a job description."
        );

        return;
    }


    prepareLiveLoadingState();

    startAnalysisTimer();

    setLoading(true);


    try {

        const form =
            new FormData();

        form.append(
            "resume",
            file
        );

        form.append(
            "jobDescription",
            jd
        );


        const response =
            await fetch(
                "/api/resume/analyze",
                {
                    method: "POST",
                    body: form
                }
            );


        let body;


        try {

            body =
                await response.json();

        } catch {

            throw new Error(
                "The server returned an unexpected response."
            );
        }


        if (!response.ok) {

            throw new Error(
                friendlyError(
                    body.message
                )
            );
        }


        stopAnalysisTimer();

        lastAnalysis =
            body;


        await showCompletionState(
            false
        );


        renderAnalysis(
            body
        );


        document
            .getElementById(
                "analysisTypeBadge"
            )
            .textContent =
                "CLAUDE LIVE";


        updateDurationBadge(
            lastAnalysisDurationMs
        );


        updateAnalysisDate();


        setLoading(false);

        showResults();


    } catch (error) {

        stopAnalysisTimer();

        setLoading(false);

        showMessage(
            error.message
            || "Unable to complete analysis. Please try again."
        );
    }
}


/* ========================================================= */
/* TIMER */
/* ========================================================= */

function startAnalysisTimer() {

    analysisStartedAt =
        performance.now();

    lastAnalysisDurationMs =
        0;

    updateElapsedTimer();

    elapsedTimerHandle =
        setInterval(
            updateElapsedTimer,
            250
        );
}


function stopAnalysisTimer() {

    if (
        analysisStartedAt !== null
    ) {

        lastAnalysisDurationMs =
            performance.now()
            - analysisStartedAt;
    }


    if (
        elapsedTimerHandle
    ) {

        clearInterval(
            elapsedTimerHandle
        );

        elapsedTimerHandle =
            null;
    }


    updateElapsedTimer();

    analysisStartedAt =
        null;
}


function updateElapsedTimer() {

    if (!elapsedTimer) {
        return;
    }


    let elapsedMs =
        lastAnalysisDurationMs;


    if (
        analysisStartedAt !== null
    ) {

        elapsedMs =
            performance.now()
            - analysisStartedAt;
    }


    elapsedTimer.textContent =
        formatElapsedTime(
            elapsedMs
        );
}


function formatElapsedTime(
    milliseconds
) {

    const totalSeconds =
        Math.floor(
            milliseconds / 1000
        );


    const minutes =
        Math.floor(
            totalSeconds / 60
        );


    const seconds =
        totalSeconds % 60;


    return (
        String(minutes)
            .padStart(2, "0")
        +
        ":"
        +
        String(seconds)
            .padStart(2, "0")
    );
}


function updateDurationBadge(
    milliseconds
) {

    if (!analysisDurationBadge) {
        return;
    }


    const seconds =
        milliseconds / 1000;


    analysisDurationBadge.textContent =
        seconds < 60
            ? `Completed in ${seconds.toFixed(1)}s`
            : `Completed in ${formatElapsedTime(milliseconds)}`;
}


/* ========================================================= */
/* ANALYSIS DATE */
/* ========================================================= */

function updateAnalysisDate() {

    if (!analysisDateBadge) {
        return;
    }


    const now =
        new Date();


    analysisDateBadge.textContent =
        now.toLocaleString(
            "en-US",
            {
                month: "short",
                day: "numeric",
                year: "numeric",
                hour: "numeric",
                minute: "2-digit"
            }
        );
}


/* ========================================================= */
/* DEMO MODE */
/* ========================================================= */

async function runDemo() {

    clearMessage();

    resetAnalyzerForDemo();


    document
        .getElementById("analyzer")
        .scrollIntoView({
            behavior: "smooth",
            block: "start"
        });


    await wait(
        550
    );


    prepareDemoLoadingState();

    loadingOverlay.classList.remove(
        "hidden"
    );


    const demoStarted =
        performance.now();


    await runDemoStep(
        0,
        "Reading resume and job description...",
        850
    );


    await runDemoStep(
        1,
        "Understanding role requirements...",
        850
    );


    await runDemoStep(
        2,
        "Matching candidate evidence...",
        850
    );


    await runDemoStep(
        3,
        "Generating hiring recommendations...",
        900
    );


    lastAnalysisDurationMs =
        performance.now()
        - demoStarted;


    await showCompletionState(
        true
    );


    const sample =
        createDemoAnalysis();


    lastAnalysis =
        sample;


    renderAnalysis(
        sample
    );


    document
        .getElementById(
            "analysisTypeBadge"
        )
        .textContent =
            "SAMPLE DEMO";


    if (analysisDurationBadge) {

        analysisDurationBadge.textContent =
            `Demo completed in ${(lastAnalysisDurationMs / 1000).toFixed(1)}s`;
    }


    updateAnalysisDate();


    loadingOverlay.classList.add(
        "hidden"
    );


    restoreNormalLoadingState();


    showResults();


    showSuccessMessage(
        "Sample analysis completed. No AI credits were used."
    );
}


/* ========================================================= */
/* DEMO STEP ANIMATION */
/* ========================================================= */

async function runDemoStep(
    activeIndex,
    status,
    duration
) {

    const steps =
        getLoadingSteps();


    steps.forEach(
        (element, index) => {

            element.classList.toggle(
                "active",
                index === activeIndex
            );


            element.classList.toggle(
                "completed",
                index < activeIndex
            );
        }
    );


    if (loadingStatusText) {

        loadingStatusText.textContent =
            status;
    }


    if (elapsedTimer) {

        elapsedTimer.textContent =
            formatElapsedTime(
                performance.now()
                - demoAnimationStartedAt
            );
    }


    const timerStart =
        performance.now();


    while (
        performance.now()
        - timerStart
        < duration
    ) {

        if (elapsedTimer) {

            elapsedTimer.textContent =
                formatElapsedTime(
                    performance.now()
                    - demoAnimationStartedAt
                );
        }


        await wait(
            100
        );
    }
}


let demoAnimationStartedAt =
    0;


function prepareDemoLoadingState() {

    demoAnimationStartedAt =
        performance.now();


    restoreNormalLoadingState();


    if (loadingTitle) {

        loadingTitle.textContent =
            "Demo Candidate Analysis";
    }


    if (loadingDescription) {

        loadingDescription.textContent =
            "Running a sample candidate-job evaluation using pre-generated demo data.";
    }


    if (typicalAnalysisTime) {

        typicalAnalysisTime.textContent =
            "Demo completes in a few seconds and uses no AI credits.";
    }


    if (elapsedTimer) {

        elapsedTimer.textContent =
            "00:00";
    }


    resetLoadingSteps();
}


/* ========================================================= */
/* LIVE LOADING STATE */
/* ========================================================= */

function prepareLiveLoadingState() {

    restoreNormalLoadingState();


    if (loadingTitle) {

        loadingTitle.textContent =
            "Claude AI Live Analysis";
    }


    if (loadingDescription) {

        loadingDescription.textContent =
            "Claude is evaluating candidate-job fit using resume evidence and job requirements.";
    }


    if (typicalAnalysisTime) {

        typicalAnalysisTime.textContent =
            "Typical analysis time: 30–60 seconds";
    }


    resetLoadingSteps();
}


/* ========================================================= */
/* COMPLETION STATE */
/* ========================================================= */

async function showCompletionState(
    demoMode
) {

    stopLoadingSteps();


    getLoadingSteps()
        .forEach(
            element => {

                element.classList.remove(
                    "active"
                );

                element.classList.add(
                    "completed"
                );
            }
        );


    if (loadingSpinner) {

        loadingSpinner.classList.add(
            "hidden"
        );
    }


    if (analysisCompleteIcon) {

        analysisCompleteIcon.classList.remove(
            "hidden"
        );
    }


    if (loadingCard) {

        loadingCard.classList.add(
            "completing"
        );
    }


    if (loadingTitle) {

        loadingTitle.textContent =
            "Analysis Complete";
    }


    if (loadingDescription) {

        loadingDescription.textContent =
            demoMode
                ? "Sample candidate intelligence report is ready."
                : "Candidate intelligence report is ready.";
    }


    if (typicalAnalysisTime) {

        typicalAnalysisTime.textContent =
            "Preparing dashboard...";
    }


    if (loadingStatusText) {

        loadingStatusText.textContent =
            "Preparing candidate intelligence report...";
    }


    await wait(
        750
    );
}


/* ========================================================= */
/* RESTORE LOADING UI */
/* ========================================================= */

function restoreNormalLoadingState() {

    if (loadingSpinner) {

        loadingSpinner.classList.remove(
            "hidden"
        );
    }


    if (analysisCompleteIcon) {

        analysisCompleteIcon.classList.add(
            "hidden"
        );
    }


    if (loadingCard) {

        loadingCard.classList.remove(
            "completing"
        );
    }
}


/* ========================================================= */
/* DEMO RESET */
/* ========================================================= */

function resetAnalyzerForDemo() {

    results.classList.add(
        "hidden"
    );


    clearMessage();


    lastAnalysis =
        null;


    setProgress(
        "matchProgress",
        0
    );


    setProgress(
        "atsProgress",
        0
    );


    setProgress(
        "confidenceProgress",
        0
    );
}


/* ========================================================= */
/* DEMO DATA */
/* ========================================================= */

function createDemoAnalysis() {

    return {

        matchScore: 91,

        atsScore: 89,

        confidenceScore: 96,

        hiringRecommendation:
            "STRONG MATCH",

        executiveAssessment:
            "The candidate demonstrates strong alignment with a senior Java/AWS engineering role through extensive hands-on experience with Java, Spring Boot, cloud-native AWS services, microservices, distributed systems, infrastructure as code, secure API integrations, and technical leadership. Some preferred technologies are not directly demonstrated, but substantial transferable experience reduces the overall hiring risk.",

        matchedSkills: [
            "Java 17",
            "Spring Boot",
            "AWS",
            "Microservices",
            "REST APIs",
            "Terraform",
            "Redis",
            "Event-Driven Architecture",
            "Technical Leadership"
        ],

        missingSkills: [
            "Kubernetes production experience",
            "Apache Kafka production experience",
            "Spring Cloud"
        ],

        criticalGaps: [
            "No direct evidence of production Kubernetes ownership."
        ],

        preferredGaps: [
            "Spring Cloud",
            "Production Kafka"
        ],

        transferableSkills: [
            "ECS/Fargate experience transfers strongly to container orchestration concepts.",
            "SQS and EventBridge experience transfers to event-streaming architecture.",
            "CloudWatch and production support experience transfers to broader observability practices."
        ],

        strengths: [
            "Deep Java and Spring Boot engineering experience.",
            "Strong AWS architecture and infrastructure ownership.",
            "Demonstrated technical leadership of an 8-engineer team.",
            "Measurable performance and fraud-reduction outcomes.",
            "Production support, security remediation, and disaster-recovery ownership."
        ],

        weaknesses: [
            "Some preferred cloud-native technologies are listed without project-level evidence.",
            "AI-assisted engineering leadership is emerging rather than demonstrated at organizational scale."
        ],

        atsRecommendations: [
            "Emphasize application resiliency where supported by production and disaster-recovery experience.",
            "Add Secure SDLC terminology where supported by SAST and Snyk remediation work.",
            "Describe AI-assisted engineering experience contextually rather than listing only tool names."
        ],

        resumeEvidence: [
            "Java 17 / Spring Boot supported by implementation of core digital-banking microservices.",
            "AWS supported by Lambda, ECS/Fargate, VPC, SQS, EventBridge, and Terraform infrastructure ownership.",
            "Leadership supported by end-to-end delivery responsibility for an 8-engineer team.",
            "Performance supported by reducing REST API response time from approximately 5 seconds to approximately 2 seconds.",
            "Security supported by Snyk/SAST remediation on a regulated banking platform."
        ],

        improvedSummary:
            "Principal Engineer with extensive experience designing and delivering cloud-native Java/Spring Boot applications on AWS. Experienced in microservices, distributed systems, event-driven architecture, secure API integration, infrastructure as code, production resiliency, and technical leadership across regulated enterprise environments.",

        improvedBullets: [
            "Led end-to-end delivery of a cloud-native digital banking platform, guiding an 8-engineer team through architecture, development, release, and production support.",
            "Designed Java 17 / Spring Boot microservices and AWS Lambda/Fargate services supporting identity verification, fraud prevention, and secure third-party integrations.",
            "Optimized REST APIs using Aurora MySQL and Redis caching, reducing average response time from approximately 5 seconds to approximately 2 seconds.",
            "Engineered asynchronous event-driven workflows using Amazon SQS and EventBridge for transaction-processing pipelines.",
            "Developed Terraform infrastructure and automated test suites while supporting security remediation, production incidents, and disaster-recovery exercises."
        ],

        interviewFocusAreas: [
            "Prepare one detailed architecture walkthrough of a Java/Spring Boot microservice.",
            "Explain your AWS deployment and Terraform strategy.",
            "Prepare examples of production incidents and resiliency improvements.",
            "Explain how SQS/EventBridge experience maps to Kafka-style event architectures.",
            "Prepare examples of responsible AI-assisted engineering usage."
        ],

        analysisMode:
            "DEMO"
    };
}


/* ========================================================= */
/* SHOW RESULTS */
/* ========================================================= */

function showResults() {

    results.classList.remove(
        "hidden"
    );


    setTimeout(
        () => {

            results.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });

        },
        100
    );
}


/* ========================================================= */
/* RENDER */
/* ========================================================= */

function renderAnalysis(data) {

    renderScores(
        data
    );


    renderRecruiterSnapshot(
        data
    );


    text(
        "executiveAssessment",
        data.executiveAssessment
    );


    chips(
        "matchedSkills",
        data.matchedSkills
    );


    chips(
        "missingSkills",
        data.missingSkills
    );


    chips(
        "criticalGaps",
        data.criticalGaps
    );


    chips(
        "preferredGaps",
        data.preferredGaps
    );


    chips(
        "transferableSkills",
        data.transferableSkills
    );


    list(
        "strengths",
        data.strengths
    );


    list(
        "weaknesses",
        data.weaknesses
    );


    list(
        "resumeEvidence",
        data.resumeEvidence
    );


    list(
        "atsRecommendations",
        data.atsRecommendations
    );


    text(
        "improvedSummary",
        data.improvedSummary
    );


    list(
        "improvedBullets",
        data.improvedBullets
    );


    orderedList(
        "interviewFocusAreas",
        data.interviewFocusAreas
    );
}


/* ========================================================= */
/* RECRUITER SNAPSHOT */
/* ========================================================= */

function renderRecruiterSnapshot(
    data
) {

    const match =
        normalizeScore(
            data.matchScore
        );


    text(
        "interviewReadiness",
        interviewReadinessLabel(
            match
        )
    );


    text(
        "interviewReadinessText",
        interviewReadinessDescription(
            match
        )
    );


    simpleList(
        "topStrengths",
        safeArray(
            data.strengths
        ).slice(0, 3)
    );


    const risks =
        safeArray(
            data.criticalGaps
        ).length
            ? safeArray(
                data.criticalGaps
            )
            : safeArray(
                data.weaknesses
            );


    simpleList(
        "topRisks",
        risks.slice(0, 3)
    );
}


function interviewReadinessLabel(
    score
) {

    if (score >= 90) {
        return "Very High";
    }

    if (score >= 80) {
        return "High";
    }

    if (score >= 65) {
        return "Moderate";
    }

    return "Low";
}


function interviewReadinessDescription(
    score
) {

    if (score >= 90) {
        return "Highly competitive for interview consideration.";
    }

    if (score >= 80) {
        return "Competitive candidate with manageable gaps.";
    }

    if (score >= 65) {
        return "Potential fit with important gaps to discuss.";
    }

    return "Low alignment for this specific role.";
}


function simpleList(
    id,
    values
) {

    const element =
        document.getElementById(id);


    if (!element) {
        return;
    }


    element.innerHTML =
        "";


    const safe =
        safeArray(values);


    if (!safe.length) {

        const li =
            document.createElement(
                "li"
            );

        li.textContent =
            "No major items identified.";

        element.appendChild(
            li
        );

        return;
    }


    safe.forEach(
        value => {

            const li =
                document.createElement(
                    "li"
                );

            li.textContent =
                value;

            element.appendChild(
                li
            );
        }
    );
}


/* ========================================================= */
/* SCORES */
/* ========================================================= */

function renderScores(data) {

    const match =
        normalizeScore(
            data.matchScore
        );


    const ats =
        normalizeScore(
            data.atsScore
        );


    const confidence =
        normalizeScore(
            data.confidenceScore
        );


    const recommendation =
        recommendationForScore(
            match
        );


    text(
        "matchScore",
        `${match}%`
    );


    text(
        "atsScore",
        `${ats}%`
    );


    text(
        "confidenceScore",
        `${confidence}%`
    );


    text(
        "hiringRecommendation",
        recommendation
    );


    text(
        "matchLabel",
        scoreLabel(match)
    );


    setProgress(
        "matchProgress",
        match
    );


    setProgress(
        "atsProgress",
        ats
    );


    setProgress(
        "confidenceProgress",
        confidence
    );


    updateRecommendation(
        recommendation
    );
}


/* ========================================================= */
/* SCORE LABELS */
/* ========================================================= */

function recommendationForScore(
    score
) {

    if (score >= 90) {
        return "STRONG MATCH";
    }

    if (score >= 80) {
        return "GOOD MATCH";
    }

    if (score >= 65) {
        return "MODERATE MATCH";
    }

    return "WEAK MATCH";
}


function scoreLabel(score) {

    if (score >= 90) {
        return "Excellent alignment";
    }

    if (score >= 80) {
        return "Strong alignment";
    }

    if (score >= 65) {
        return "Moderate alignment";
    }

    return "Limited alignment";
}


/* ========================================================= */
/* RECOMMENDATION DESCRIPTION */
/* ========================================================= */

function updateRecommendation(
    recommendation
) {

    const description =
        document.getElementById(
            "recommendationDescription"
        );


    switch (
        recommendation
    ) {

        case "STRONG MATCH":

            description.textContent =
                "Competitive profile for interview consideration.";

            break;


        case "GOOD MATCH":

            description.textContent =
                "Strong alignment with manageable gaps.";

            break;


        case "MODERATE MATCH":

            description.textContent =
                "Relevant experience with meaningful gaps to address.";

            break;


        case "WEAK MATCH":

            description.textContent =
                "Significant gaps reduce fit for this role.";

            break;


        default:

            description.textContent =
                "Evidence-based recommendation.";
    }
}


/* ========================================================= */
/* TEXT */
/* ========================================================= */

function text(
    id,
    value
) {

    const element =
        document.getElementById(id);


    if (!element) {
        return;
    }


    element.textContent =
        value || "";
}


/* ========================================================= */
/* CHIPS */
/* ========================================================= */

function chips(
    id,
    values = []
) {

    const element =
        document.getElementById(id);


    if (!element) {
        return;
    }


    element.innerHTML =
        "";


    const safe =
        safeArray(values);


    if (!safe.length) {

        const span =
            document.createElement(
                "span"
            );


        span.className =
            "chip";


        span.textContent =
            "None detected";


        element.appendChild(
            span
        );


        return;
    }


    safe.forEach(
        value => {

            const span =
                document.createElement(
                    "span"
                );


            span.className =
                "chip";


            span.textContent =
                value;


            element.appendChild(
                span
            );
        }
    );
}


/* ========================================================= */
/* LISTS */
/* ========================================================= */

function list(
    id,
    values = []
) {

    const element =
        document.getElementById(id);


    if (!element) {
        return;
    }


    element.innerHTML =
        "";


    const safe =
        safeArray(values);


    if (!safe.length) {

        const li =
            document.createElement(
                "li"
            );


        li.textContent =
            "No items returned.";


        element.appendChild(
            li
        );


        return;
    }


    safe.forEach(
        value => {

            const li =
                document.createElement(
                    "li"
                );


            li.textContent =
                value;


            element.appendChild(
                li
            );
        }
    );
}


function orderedList(
    id,
    values = []
) {

    list(
        id,
        values
    );
}


function safeArray(
    value
) {

    return Array.isArray(value)
        ? value
        : [];
}


/* ========================================================= */
/* PROGRESS */
/* ========================================================= */

function setProgress(
    id,
    value
) {

    const bar =
        document.getElementById(id);


    if (!bar) {
        return;
    }


    bar.style.width =
        `${normalizeScore(value)}%`;
}


function normalizeScore(value) {

    const number =
        Number(value);


    if (
        Number.isNaN(number)
    ) {

        return 0;
    }


    return Math.max(
        0,
        Math.min(
            100,
            Math.round(number)
        )
    );
}


/* ========================================================= */
/* LIVE LOADING STEPS */
/* ========================================================= */

function setLoading(
    loading
) {

    analyzeButton.disabled =
        loading;


    if (loading) {

        analyzeButton.textContent =
            "Analyzing…";


        loadingOverlay.classList.remove(
            "hidden"
        );


        startLoadingSteps();

    } else {

        analyzeButton.textContent =
            "Analyze with Claude";


        loadingOverlay.classList.add(
            "hidden"
        );


        stopLoadingSteps();


        restoreNormalLoadingState();
    }
}


function startLoadingSteps() {

    stopLoadingSteps();


    resetLoadingSteps();


    const steps = [
        {
            id: "loadingStep1",
            status: "Reading resume and job description..."
        },
        {
            id: "loadingStep2",
            status: "Understanding role requirements..."
        },
        {
            id: "loadingStep3",
            status: "Matching candidate evidence..."
        },
        {
            id: "loadingStep4",
            status: "Generating hiring recommendations..."
        }
    ];


    let index =
        0;


    activateLoadingStep(
        steps,
        index
    );


    loadingStepTimer =
        setInterval(
            () => {

                if (
                    index <
                    steps.length - 1
                ) {

                    index++;

                    activateLoadingStep(
                        steps,
                        index
                    );
                }

            },
            9000
        );
}


function stopLoadingSteps() {

    if (
        loadingStepTimer
    ) {

        clearInterval(
            loadingStepTimer
        );


        loadingStepTimer =
            null;
    }
}


function resetLoadingSteps() {

    getLoadingSteps()
        .forEach(
            element => {

                element.classList.remove(
                    "active",
                    "completed"
                );
            }
        );


    const first =
        document.getElementById(
            "loadingStep1"
        );


    if (first) {

        first.classList.add(
            "active"
        );
    }


    if (loadingStatusText) {

        loadingStatusText.textContent =
            "Reading resume and job description...";
    }
}


function getLoadingSteps() {

    return [
        document.getElementById(
            "loadingStep1"
        ),
        document.getElementById(
            "loadingStep2"
        ),
        document.getElementById(
            "loadingStep3"
        ),
        document.getElementById(
            "loadingStep4"
        )
    ].filter(Boolean);
}


function activateLoadingStep(
    steps,
    activeIndex
) {

    steps.forEach(
        (step, index) => {

            const element =
                document.getElementById(
                    step.id
                );


            if (!element) {
                return;
            }


            element.classList.toggle(
                "active",
                index === activeIndex
            );


            element.classList.toggle(
                "completed",
                index < activeIndex
            );
        }
    );


    if (loadingStatusText) {

        loadingStatusText.textContent =
            steps[activeIndex].status;
    }
}


/* ========================================================= */
/* ERROR */
/* ========================================================= */

function friendlyError(
    rawMessage
) {

    if (!rawMessage) {

        return "Unable to contact the AI service. Please try again.";
    }


    const lower =
        rawMessage.toLowerCase();


    if (
        lower.includes("invalid structured response")
        || lower.includes("unexpected end-of-input")
        || lower.includes("json")
    ) {

        return "The AI returned an incomplete response. Please try the analysis again.";
    }


    if (
        lower.includes("429")
        || lower.includes("rate")
    ) {

        return "The AI service is temporarily busy. Please wait a moment and try again.";
    }


    if (
        lower.includes("401")
        || lower.includes("authentication")
        || lower.includes("api key")
    ) {

        return "The AI service is temporarily unavailable. Please try Demo Mode instead.";
    }


    if (
        lower.includes("timeout")
    ) {

        return "The analysis took longer than expected. Please try again.";
    }


    return "Unable to complete the AI analysis. Please try again or use Demo Mode.";
}


/* ========================================================= */
/* COPY */
/* ========================================================= */

async function copyText(id) {

    const element =
        document.getElementById(id);


    if (!element) {
        return;
    }


    const value =
        element.innerText.trim();


    if (!value) {

        showMessage(
            "Nothing available to copy."
        );


        return;
    }


    try {

        await navigator
            .clipboard
            .writeText(
                value
            );


        showSuccessMessage(
            "Copied to clipboard."
        );

    } catch {

        showMessage(
            "Unable to copy."
        );
    }
}


async function copyList(id) {

    const element =
        document.getElementById(id);


    if (!element) {
        return;
    }


    const items =
        [
            ...element.querySelectorAll(
                "li"
            )
        ];


    const value =
        items
            .map(
                item =>
                    `• ${item.innerText.trim()}`
            )
            .join("\n");


    if (!value) {

        showMessage(
            "Nothing available to copy."
        );


        return;
    }


    try {

        await navigator
            .clipboard
            .writeText(
                value
            );


        showSuccessMessage(
            "Copied to clipboard."
        );

    } catch {

        showMessage(
            "Unable to copy."
        );
    }
}


/* ========================================================= */
/* EXPORT JSON */
/* ========================================================= */

downloadJsonButton.addEventListener(
    "click",
    () => {

        if (!lastAnalysis) {

            showMessage(
                "Run an analysis before exporting."
            );


            return;
        }


        const blob =
            new Blob(
                [
                    JSON.stringify(
                        lastAnalysis,
                        null,
                        2
                    )
                ],
                {
                    type:
                        "application/json"
                }
            );


        const url =
            URL.createObjectURL(
                blob
            );


        const link =
            document.createElement(
                "a"
            );


        link.href =
            url;


        link.download =
            `candidate-analysis-${dateStamp()}.json`;


        document.body
            .appendChild(
                link
            );


        link.click();


        link.remove();


        URL.revokeObjectURL(
            url
        );
    }
);


/* ========================================================= */
/* RESET */
/* ========================================================= */

resetButton.addEventListener(
    "click",
    resetAnalyzer
);


analyzeAnotherButton.addEventListener(
    "click",
    () => {

        resetAnalyzer();


        document
            .getElementById(
                "analyzer"
            )
            .scrollIntoView({
                behavior:
                    "smooth"
            });
    }
);


function resetAnalyzer() {

    stopAnalysisTimer();

    stopLoadingSteps();


    resumeInput.value =
        "";


    jobDescription.value =
        "";


    fileName.textContent =
        "Maximum 10 MB";


    results.classList.add(
        "hidden"
    );


    lastAnalysis =
        null;


    clearMessage();


    setProgress(
        "matchProgress",
        0
    );


    setProgress(
        "atsProgress",
        0
    );


    setProgress(
        "confidenceProgress",
        0
    );


    if (analysisDurationBadge) {

        analysisDurationBadge.textContent =
            "Completed in --";
    }


    if (analysisDateBadge) {

        analysisDateBadge.textContent =
            "--";
    }


    if (elapsedTimer) {

        elapsedTimer.textContent =
            "00:00";
    }


    text(
        "interviewReadiness",
        "-"
    );


    text(
        "interviewReadinessText",
        "Waiting for analysis"
    );


    restoreNormalLoadingState();
}


/* ========================================================= */
/* MESSAGES */
/* ========================================================= */

function showMessage(value) {

    message.classList.remove(
        "success-message"
    );


    message.textContent =
        value || "";
}


function showSuccessMessage(value) {

    message.classList.add(
        "success-message"
    );


    message.textContent =
        value;


    setTimeout(
        () => {

            if (
                message.textContent
                === value
            ) {

                clearMessage();
            }

        },
        3000
    );
}


function clearMessage() {

    message.textContent =
        "";


    message.classList.remove(
        "success-message"
    );
}


/* ========================================================= */
/* HELPERS */
/* ========================================================= */

function wait(
    milliseconds
) {

    return new Promise(
        resolve =>
            setTimeout(
                resolve,
                milliseconds
            )
    );
}


function dateStamp() {

    const now =
        new Date();


    const year =
        now.getFullYear();


    const month =
        String(
            now.getMonth() + 1
        )
        .padStart(
            2,
            "0"
        );


    const day =
        String(
            now.getDate()
        )
        .padStart(
            2,
            "0"
        );


    return `${year}-${month}-${day}`;
}