document.addEventListener("DOMContentLoaded", function() {
    const steps = document.querySelectorAll('.form-step');
    let currentStep = 0;
    let uploadedFiles = {};
    let optionalFiles = [];
    let requestContent = "";
    let selectedPlatforms = [];

    let deceasedId = null;

    const startBtn = document.getElementById('startBtn');
    const optionalDocNextBtn = document.getElementById('optionalDocNextBtn');
    const platformNextBtn = document.getElementById('platformNextBtn');
    const contentNextBtn = document.getElementById('contentNextBtn');
    const requestContentInput = document.getElementById('request-content');
    const editBtn = document.getElementById('editBtn');
    const submitBtn = document.getElementById('submitBtn');
    const resetBtn = document.getElementById('resetBtn');


    function initializeStep() {
        const urlParams = new URLSearchParams(window.location.search);
        const step = urlParams.get('step');

        deceasedId = urlParams.get('targetId');

        if (step === 'upload' && deceasedId) {
            goToStep(4);
            return;
        }

        showStep(0);
    }

    function showStep(stepIndex) {
        steps.forEach((step, index) => {
            step.style.display = index === stepIndex ? 'flex' : 'none';
            setTimeout(() => {
                if (index === stepIndex) step.classList.remove('hidden');
            }, 10);
        });

        const currentStepId = steps[stepIndex] ? steps[stepIndex].id : null;
        if (currentStepId === 'step-greeting') {
            setTimeout(() => nextStep(), 2000);
        } else if (currentStepId === 'step-docs-intro') {
            setTimeout(() => nextStep(), 3000);
        }
    }

    function goToStep(stepIndex) {
        if (steps[currentStep]) steps[currentStep].classList.add('hidden');
        setTimeout(() => {
            currentStep = stepIndex;
            showStep(currentStep);
        }, 500);
    }

    function nextStep() {
        if (currentStep < steps.length - 1) {
            goToStep(currentStep + 1);
        }
    }

    startBtn.addEventListener('click', () => {
        if (currentUser) {
            nextStep();
        } else {
            alert('로그인 후 이용할 수 있습니다.');
            window.location.href = '/user/login';
        }
    });

    document.querySelectorAll('#dropzone-doc1, #dropzone-doc2, #dropzone-doc3').forEach(zone => {
        const fileInput = zone.querySelector('.file-input');
        const fileStatus = zone.querySelector('.file-status');
        const prompt = zone.querySelector('p');
        const stepId = zone.closest('.form-step').id;

        const handleFile = (files) => {
            if (files.length === 0) return;
            const file = files[0];
            prompt.style.display = 'none';
            fileStatus.textContent = `✅ ${file.name} (업로드 완료)`;
            uploadedFiles[stepId] = file;
            setTimeout(() => nextStep(), 1000);
        };

        zone.addEventListener('click', () => fileInput.click());
        fileInput.addEventListener('change', () => handleFile(fileInput.files));
        zone.addEventListener('dragover', (e) => { e.preventDefault(); zone.classList.add('drag-over'); });
        zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));
        zone.addEventListener('drop', (e) => {
            e.preventDefault();
            zone.classList.remove('drag-over');
            handleFile(e.dataTransfer.files);
        });
    });

    const optionalDropZone = document.getElementById('dropzone-optional');
    const optionalFileInput = optionalDropZone.querySelector('.file-input');
    const optionalPrompt = optionalDropZone.querySelector('p');
    const optionalFileList = document.getElementById('optional-file-list');

    function handleOptionalFiles(files) {
        if (files.length === 0) return;
        optionalPrompt.style.display = 'none';
        for (const file of files) {
            optionalFiles.push(file);
            const li = document.createElement('li');
            li.textContent = `✅ ${file.name}`;
            optionalFileList.appendChild(li);
        }
    }

    optionalDropZone.addEventListener('click', () => optionalFileInput.click());
    optionalFileInput.addEventListener('change', () => handleOptionalFiles(optionalFileInput.files));
    optionalDropZone.addEventListener('dragover', (e) => { e.preventDefault(); optionalDropZone.classList.add('drag-over'); });
    optionalDropZone.addEventListener('dragleave', () => optionalDropZone.classList.remove('drag-over'));
    optionalDropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        optionalDropZone.classList.remove('drag-over');
        handleOptionalFiles(e.dataTransfer.files);
    });
    optionalDocNextBtn.addEventListener('click', nextStep);

    document.querySelectorAll('.platform-btn').forEach(button => {
        button.addEventListener('click', () => {
            button.classList.toggle('active');
            const platform = button.dataset.platform;
            if (selectedPlatforms.includes(platform)) {
                selectedPlatforms = selectedPlatforms.filter(p => p !== platform);
            } else {
                selectedPlatforms.push(platform);
            }
        });
    });

    platformNextBtn.addEventListener('click', nextStep);

    contentNextBtn.addEventListener('click', () => {
        requestContent = requestContentInput.value.trim();

        document.getElementById('platform-summary').textContent = selectedPlatforms.join(', ');
        const fileSummaryList = document.getElementById('file-summary-list');
        fileSummaryList.innerHTML = '';
        Object.values(uploadedFiles).forEach(file => {
            const li = document.createElement('li');
            li.textContent = `✅ ${file.name} (필수)`;
            fileSummaryList.appendChild(li);
        });
        optionalFiles.forEach(file => {
            const li = document.createElement('li');
            li.textContent = `✅ ${file.name} (기타)`;
            fileSummaryList.appendChild(li);
        });
        document.getElementById('content-summary').textContent = requestContent || "작성된 내용이 없습니다.";

        nextStep();
    });

    editBtn.addEventListener('click', () => goToStep(3));

    submitBtn.addEventListener('click', async () => {
        const formData = new FormData();
        selectedPlatforms.forEach(p => formData.append('platforms', p));
        formData.append('relationCertification', uploadedFiles['step-doc1']);
        formData.append('deathCertificate', uploadedFiles['step-doc2']);
        formData.append('applicantId', uploadedFiles['step-doc3']);
        optionalFiles.forEach(file => formData.append('otherFiles', file));
        formData.append('reason', requestContent);

        if (!deceasedId) {
            alert('고인 정보가 올바르지 않습니다. 처음부터 다시 시도해주세요.');
            window.location.href = '/deceased';
            return;
        }
        formData.append('deceasedId', deceasedId);

        nextStep();
        const loadingText = document.getElementById('loading-text');

        try {
            const response = await fetch('/requests/apply', { method: 'POST', body: formData, credentials: 'include' });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error('Server error: ' + errorText);
            }
        } catch (error) {
            console.error('Submission failed:', error);
            alert('제출에 실패했습니다. 서버 로그를 확인해주세요.');
            goToStep(8);
            return;
        }

        setTimeout(() => {
            loadingText.textContent = '각 플랫폼에 요청을 보내고 있습니다.';
        }, 2000);
        setTimeout(() => {
            nextStep();
        }, 4000);
    });

    resetBtn.addEventListener('click', () => window.location.reload());

    initializeStep();
});