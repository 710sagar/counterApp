// Parking Entry Tracker JavaScript

class ParkingTracker {
    constructor() {
        this.currentUser = null;
        this.selectedLot = null;
        this.availableEntryTypes = [];
        this.csrfToken = $('meta[name="_csrf"]').attr('content');
        this.csrfHeader = $('meta[name="_csrf_header"]').attr('content');
        this.entryTypeMappings = {
            'REGISTERED': { label: 'Registered', class: 'btn-primary', color: 'registered' },
            'USA': { label: 'USA', class: 'btn-warning', color: 'usa' },
            'NON_REGISTERED': { label: 'Non-registered', class: 'btn-danger', color: 'non-registered' },
            'FAMILY_AREA': { label: 'Family Area', class: 'btn-success', color: 'family-area' },
            'SNP': { label: 'SNP', class: 'btn-secondary', color: 'snp' },
            'NORMAL_LOT': { label: 'Normal Lot', class: 'btn-dark', color: 'normal-lot' }
        };

        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadUserInfo();
        this.checkLotSelection();
    }

    setupEventListeners() {
        // Lot selection
        $('#changeLotBtn, #selectLotBtn').on('click', () => this.showLotSelectionModal());
        $(document).on('click', '.lot-option', (e) => this.selectLot($(e.target).data('lot')));

        // Entry actions
        $(document).on('click', '.entry-type-btn', (e) => this.addSingleEntry($(e.target).data('entry-type')));
        $('#addMultipleEntries').on('click', () => this.addMultipleEntries());
        $('#undoLastEntry').on('click', () => this.undoLastEntry());

        // Admin actions
        $('#resetEntries').on('click', () => this.resetAllEntries());
        $('#refreshData').on('click', () => this.loadData());

        // Form validation
        $('#entryCount').on('input', function() {
            const value = parseInt($(this).val());
            if (value < 1 || value > 50) {
                $(this).addClass('is-invalid');
            } else {
                $(this).removeClass('is-invalid');
            }
        });

        // Handle enter key in multiple entry form
        $('#entryCount, #multipleEntryType').on('keypress', (e) => {
            if (e.which === 13) {
                this.addMultipleEntries();
            }
        });
    }

    async loadUserInfo() {
        try {
            const response = await $.ajax({
                url: '/api/user',
                type: 'GET',
                headers: { [this.csrfHeader]: this.csrfToken }
            });
            this.currentUser = response;
            $('#username').text(response.username);
            $('#role').text(response.role);

            // Show/hide admin features
            if (response.isAdmin) {
                $('.admin-only').show();
            } else {
                $('.admin-only').hide();
            }
        } catch (error) {
            console.error('Failed to load user info:', error);
            this.showError('Failed to load user information: ' + (error.responseText || 'Unknown error'));
        }
    }

    checkLotSelection() {
        const lotElement = $('.selected-lot-display span');
        if (lotElement.length && lotElement.text().trim() !== 'None' && lotElement.text().trim() !== '') {
            this.selectedLot = lotElement.text().trim();
            this.loadAvailableEntryTypes();
        } else {
            const selectBtn = $('#selectLotBtn');
            if (selectBtn.length > 0) {
                setTimeout(() => this.showLotSelectionModal(), 500);
            }
        }
    }

    showLotSelectionModal() {
        const modal = new bootstrap.Modal(document.getElementById('lotSelectionModal'));
        modal.show();
    }

    async selectLot(lot) {
        if (!lot) return;

        this.showLoading(true);

        try {
            await $.ajax({
                url: '/api/setLot',
                type: 'POST',
                contentType: 'application/json',
                headers: { [this.csrfHeader]: this.csrfToken },
                data: JSON.stringify({ lot: lot })
            });

            this.selectedLot = lot;
            this.showSuccess(`Lot ${lot} selected successfully`);

            const modal = bootstrap.Modal.getInstance(document.getElementById('lotSelectionModal'));
            if (modal) modal.hide();

            setTimeout(() => location.reload(), 1000);

        } catch (error) {
            console.error('Failed to set lot:', error);
            this.showError('Failed to set lot: ' + (error.responseText || 'Unknown error'));
        } finally {
            this.showLoading(false);
        }
    }

    async loadAvailableEntryTypes() {
        if (!this.selectedLot) return;

        try {
            const response = await $.ajax({
                url: '/api/availableEntryTypes',
                type: 'GET',
                data: { lot: this.selectedLot },
                headers: { [this.csrfHeader]: this.csrfToken }
            });
            this.availableEntryTypes = response;
            this.renderEntryButtons();
            this.renderMultipleEntryOptions();
            this.loadData();
        } catch (error) {
            console.error('Failed to load entry types:', error);
            this.showError('Failed to load entry types: ' + (error.responseText || 'Unknown error'));
        }
    }

    renderEntryButtons() {
        const container = $('#entryButtonsContainer');
        if (!container.length || !this.availableEntryTypes.length) return;

        container.empty();

        this.availableEntryTypes.forEach(entryType => {
            const mapping = this.entryTypeMappings[entryType];
            if (!mapping) return;

            const buttonHtml = `
                <div class="col-md-6 col-lg-4">
                    <button type="button"
                            class="btn ${mapping.class} btn-lg w-100 entry-type-btn"
                            data-entry-type="${entryType}">
                        <i class="fas fa-plus-circle me-2"></i>
                        Add ${mapping.label}
                    </button>
                </div>
            `;
            container.append(buttonHtml);
        });
    }

    renderMultipleEntryOptions() {
        const select = $('#multipleEntryType');
        if (!select.length || !this.availableEntryTypes.length) return;

        select.empty();
        select.append('<option value="">Select Entry Type</option>');

        this.availableEntryTypes.forEach(entryType => {
            const mapping = this.entryTypeMappings[entryType];
            if (mapping) {
                const isSelected = entryType === 'REGISTERED' ? 'selected' : '';
                select.append(`<option value="${entryType}" ${isSelected}>${mapping.label}</option>`);
            }
        });
    }

    async addSingleEntry(entryType) {
        if (!entryType || !this.selectedLot) return;

        const button = $(`.entry-type-btn[data-entry-type="${entryType}"]`);
        button.prop('disabled', true);

        this.showLoading(true);

        try {
            await $.ajax({
                url: '/api/addEntry',
                type: 'POST',
                contentType: 'application/json',
                headers: { [this.csrfHeader]: this.csrfToken },
                data: JSON.stringify({
                    entryType: entryType,
                    count: 1
                })
            });

            const mapping = this.entryTypeMappings[entryType];
            this.showSuccess(`${mapping.label} entry added successfully`);
            this.loadData();

        } catch (error) {
            console.error('Failed to add single entry:', error);
            this.showError('Failed to add entry: ' + (error.responseText || 'Unknown error'));
        } finally {
            this.showLoading(false);
            button.prop('disabled', false);
        }
    }

    async addMultipleEntries() {
        const entryType = $('#multipleEntryType').val();
        const count = parseInt($('#entryCount').val());

        if (!entryType) {
            this.showError('Please select an entry type');
            $('#multipleEntryType').focus();
            return;
        }

        if (!count || count < 1) {
            this.showError('Please enter a valid count (1-50)');
            $('#entryCount').focus();
            return;
        }

        this.showLoading(true);

        try {
            await $.ajax({
                url: '/api/addEntry',
                type: 'POST',
                contentType: 'application/json',
                headers: { [this.csrfHeader]: this.csrfToken },
                data: JSON.stringify({
                    entryType: entryType,
                    count: count
                })
            });

            const mapping = this.entryTypeMappings[entryType];
            this.showSuccess(`${count} ${mapping.label} entries added successfully`);

            $('#multipleEntryType').val('REGISTERED');
            $('#entryCount').val('1');

            this.loadData();

        } catch (error) {
            console.error('Failed to add multiple entries:', error);
            this.showError('Failed to add entries: ' + (error.responseText || 'Unknown error'));
        } finally {
            this.showLoading(false);
        }
    }

    async undoLastEntry() {
        if (!confirm('Are you sure you want to undo your last entry?')) return;

        this.showLoading(true);

        try {
            await $.ajax({
                url: '/api/undoLastEntry',
                type: 'POST',
                headers: { [this.csrfHeader]: this.csrfToken }
            });

            this.showSuccess('Last entry undone successfully');
            this.loadData();

        } catch (error) {
            console.error('Failed to undo entry:', error);
            this.showError('Failed to undo entry: ' + (error.responseText || 'Unknown error'));
        } finally {
            this.showLoading(false);
        }
    }

    async resetAllEntries() {
        const confirmMessage = 'Are you sure you want to reset all entries for today? This will mark all entries across all lots as deleted and cannot be undone.';
        if (!confirm(confirmMessage)) return;

        if (!confirm('This is your final warning. All today\'s entries will be marked as deleted. Continue?')) return;

        this.showLoading(true);

        try {
            const response = await $.ajax({
                url: '/api/reset',
                type: 'POST',
                headers: { [this.csrfHeader]: this.csrfToken }
            });

            this.showSuccess(response);
            this.loadData();

        } catch (error) {
            console.error('Failed to reset entries:', error);
            this.showError('Failed to reset entries: ' + (error.responseText || 'Unknown error'));
        } finally {
            this.showLoading(false);
        }
    }

    async loadData() {
        if (this.currentUser && this.currentUser.isAdmin) {
            await this.loadAdminData();
        } else if (this.selectedLot) {
            await this.loadUserData();
        }
    }

    async loadAdminData() {
        try {
            const response = await $.ajax({
                url: '/api/entries/all',
                type: 'GET',
                headers: { [this.csrfHeader]: this.csrfToken }
            });
            this.renderAdminTable(response);
        } catch (error) {
            console.error('Failed to load admin data:', error);
            this.showError('Failed to load admin data: ' + (error.responseText || 'Unknown error'));
        }
    }

    async loadUserData() {
        if (!this.selectedLot) return;

        try {
            const response = await $.ajax({
                url: '/api/entries',
                type: 'GET',
                data: { lot: this.selectedLot },
                headers: { [this.csrfHeader]: this.csrfToken }
            });
            this.renderUserCounts(response);
        } catch (error) {
            console.error('Failed to load user data:', error);
            this.showError('Failed to load entry data: ' + (error.responseText || 'Unknown error'));
        }
    }

    renderAdminTable(data) {
        const tbody = $('#adminTableBody');
        if (!tbody.length || !data.allCounts) return;

        tbody.empty();

        let grandTotal = 0;

        data.gates.forEach(gate => {
            const gateCounts = data.allCounts[gate] || {};
            let gateTotal = 0;

            let row = `<tr><td class="fw-bold">${gate}</td>`;

            ['REGISTERED', 'USA', 'NON_REGISTERED', 'FAMILY_AREA', 'SNP', 'NORMAL_LOT'].forEach(type => {
                const count = gateCounts[type] || 0;
                gateTotal += count;
                row += `<td class="text-center">${count}</td>`;
            });

            grandTotal += gateTotal;
            row += `<td class="fw-bold text-center table-info">${gateTotal}</td></tr>`;
            tbody.append(row);
        });

        const totalRow = `
            <tr class="table-dark">
                <td class="fw-bold">TOTAL</td>
                <td colspan="6" class="text-center">All Entries</td>
                <td class="fw-bold text-center">${grandTotal}</td>
            </tr>
        `;
        tbody.append(totalRow);
    }

    renderUserCounts(data) {
        const container = $('#entryCountsContainer');
        if (!container.length || !data.typeCounts) return;

        container.empty();

        let totalCount = 0;

        this.availableEntryTypes.forEach(entryType => {
            const mapping = this.entryTypeMappings[entryType];
            if (!mapping) return;

            const count = data.typeCounts[entryType] || 0;
            totalCount += count;

            const cardHtml = `
                <div class="col-md-6 col-lg-4">
                    <div class="entry-count-card ${mapping.color} fade-in">
                        <span class="count-number">${count}</span>
                        <span class="count-label">${mapping.label}</span>
                    </div>
                </div>
            `;
            container.append(cardHtml);
        });

        const totalCardHtml = `
            <div class="col-md-6 col-lg-4">
                <div class="entry-count-card total-count fade-in">
                    <span class="count-number">${totalCount}</span>
                    <span class="count-label">Total Entries</span>
                </div>
            </div>
        `;
        container.append(totalCardHtml);
    }

    showSuccess(message) {
        $('#successMessage').text(message);
        const toast = new bootstrap.Toast(document.getElementById('successToast'));
        toast.show();
    }

    showError(message) {
        $('#errorMessage').text(message);
        const toast = new bootstrap.Toast(document.getElementById('errorToast'));
        toast.show();
    }

    showLoading(show) {
        if (show) {
            $('#loadingSpinner').removeClass('d-none');
        } else {
            $('#loadingSpinner').addClass('d-none');
        }
    }
}

$(document).ready(function() {
    window.parkingTracker = new ParkingTracker();

    // Auto-refresh data every 30 seconds
    setInterval(() => {
        if (window.parkingTracker) {
            window.parkingTracker.loadData();
        }
    }, 30000);

    // Keyboard shortcuts
    $(document).on('keypress', function(e) {
        if (e.ctrlKey || e.metaKey) {
            switch(e.which) {
                case 114: // Ctrl+R - Refresh
                    e.preventDefault();
                    window.parkingTracker.loadData();
                    break;
                case 108: // Ctrl+L - Change Lot
                    e.preventDefault();
                    window.parkingTracker.showLotSelectionModal();
                    break;
            }
        }
    });
});