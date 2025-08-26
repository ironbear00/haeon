document.addEventListener("DOMContentLoaded", function() {
    const steps = document.querySelectorAll('.form-step');
    let currentStep = 0;
    let uploadedFiles = {};
    let selectedPlatforms = [];

    const startBtn = document.getElementById('startBtn');
    const platformNextBtn = document.getElementById('platformNextBtn');
    const editBtn = document.getElementById('editBtn');
    const submitBtn = document.getElementById('submitBtn');
    const resetBtn = document.getElementById('resetBtn');

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
        nextStep();
    });

    document.querySelectorAll('.drop-zone').forEach(zone => {
        const fileInput = zone.querySelector('.file-input');
        const fileStatus = zone.querySelector('.file-status');
        const prompt = zone.querySelector('p');
        const stepId = zone.closest('.form-step').id;

        const handleFile = (files) => {
            if (files.length === 0) return;
            const file = files[0];
            prompt.style.display = 'none';
            fileStatus.textContent = `✅ ${file.name} (업로드 완료)`;
            uploadedFiles[stepId] = file; // ★ 수정: 파일 객체 자체를 저장
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

    platformNextBtn.addEventListener('click', () => {
        if (selectedPlatforms.length === 0) {
            alert('하나 이상의 플랫폼을 선택해주세요.');
            return;
        }
        document.getElementById('platform-summary').textContent = selectedPlatforms.join(', ');
        const fileList = document.getElementById('file-summary-list');
        fileList.innerHTML = '';
        Object.values(uploadedFiles).forEach(file => { // ★ 수정: 객체를 순회하며 파일 이름 표시
            const li = document.createElement('li');
            li.textContent = `✅ ${file.name}`;
            fileList.appendChild(li);
        });
        nextStep();
    });

    editBtn.addEventListener('click', () => goToStep(3));

    submitBtn.addEventListener('click', async () => {
        const formData = new FormData();
        selectedPlatforms.forEach(p => formData.append('platforms', p));
        formData.append('relationCertification', uploadedFiles['step-doc1']);
        formData.append('deathCertificate', uploadedFiles['step-doc2']);
        formData.append('applicantId', uploadedFiles['step-doc3']);

        nextStep(); // -> Loading
        const loadingText = document.getElementById('loading-text');

        try {
            const response = await fetch('/requests/apply',
                { method: 'POST',
                    body: formData,
                    credentials: 'include'
                });
            if (!response.ok) throw new Error('Server error');
        } catch (error) {
            alert('제출에 실패했습니다.');
            goToStep(7);
            return;
        }

        setTimeout(() => {
            loadingText.textContent = '각 플랫폼에 요청을 보내고 있습니다.';
        }, 2000);
        setTimeout(() => {
            nextStep(); // -> Complete
        }, 4000);
    });

    resetBtn.addEventListener('click', () => window.location.reload());

    showStep(0);
});