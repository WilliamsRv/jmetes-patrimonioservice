package pe.edu.vallegrande.patrimonio_service.application.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.DepreciationUseCase;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.DepreciationPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.DepreciationNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.domain.model.Depreciation;
import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DepreciationService implements DepreciationUseCase {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final DepreciationPersistencePort persistencePort;
    private final AssetPersistencePort assetPersistencePort;

    public DepreciationService(DepreciationPersistencePort persistencePort, AssetPersistencePort assetPersistencePort) {
        this.persistencePort = persistencePort;
        this.assetPersistencePort = assetPersistencePort;
    }

    @Override
    public Mono<DepreciationResponse> createInitialDepreciation(Asset asset) {
        // Validaciones
        if (asset.getAcquisitionValue() == null
                || asset.getAcquisitionValue().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException(
                    "Asset must have a valid acquisitionValue for depreciation"));
        }

        if (asset.getUsefulLife() == null || asset.getUsefulLife() <= 0) {
            return Mono.error(new IllegalArgumentException(
                    "Asset must have a valid usefulLife (greater than 0) for depreciation"));
        }

        // Fecha de adquisición o fecha actual
        LocalDateTime acquisitionDate = asset.getAcquisitionDate() != null
                ? asset.getAcquisitionDate().atStartOfDay()
                : LocalDateTime.now();

        // Vida útil en meses
        int usefulLifeMonths = asset.getUsefulLife();

        BigDecimal residualValue = asset.getResidualValue() != null ? asset.getResidualValue()
                : BigDecimal.ZERO;

        // Generar todo el historial de depreciación automáticamente
        return generateAutomaticDepreciations(
                asset.getId(),
                asset.getAcquisitionValue(),
                residualValue,
                usefulLifeMonths,
                acquisitionDate)
                .next()
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("No depreciation records were generated")))
                .flatMap(dep -> updateAssetFromDepreciation(asset.getId(), dep));
    }

    @Override
    public Mono<Void> recalculateForAsset(UUID assetId) {
        return assetPersistencePort.findById(assetId)
                .switchIfEmpty(Mono.error(new RuntimeException("Asset not found")))
                .flatMap(asset -> persistencePort.findByAssetId(assetId)
                        .collectList()
                        .flatMap(deps -> {
                            boolean hasApproved = deps.stream()
                                    .anyMatch(d -> "APPROVED".equals(d.getCalculationStatus()));
                            if (hasApproved) {
                                return handleApprovedDepreciationAdjustment(asset, deps);
                            }
                            return persistencePort.deleteByAssetId(assetId)
                                    .then(regenerateDepreciation(asset));
                        }));
    }

    private Mono<Void> handleApprovedDepreciationAdjustment(Asset asset,
            java.util.List<Depreciation> existingDeps) {
        LocalDateTime acquisitionDate = asset.getAcquisitionDate() != null
                ? asset.getAcquisitionDate().atStartOfDay()
                : LocalDateTime.now();
        int usefulLifeMonths = asset.getUsefulLife() != null ? asset.getUsefulLife() : 0;
        BigDecimal residualValue = asset.getResidualValue() != null ? asset.getResidualValue()
                : BigDecimal.ZERO;

        if (asset.getAcquisitionValue() == null || asset.getAcquisitionValue().compareTo(BigDecimal.ZERO) <= 0
                || usefulLifeMonths <= 0) {
            return Mono.empty();
        }

        BigDecimal totalDepreciable = asset.getAcquisitionValue().subtract(residualValue);
        BigDecimal correctMonthlyDep = totalDepreciable.divide(
                BigDecimal.valueOf(usefulLifeMonths), 10, RoundingMode.HALF_UP);

        BigDecimal correctAccumulated = BigDecimal.ZERO;
        BigDecimal registeredAccumulated = BigDecimal.ZERO;

        for (Depreciation dep : existingDeps) {
            int monthIndex = (int) java.time.temporal.ChronoUnit.MONTHS.between(
                    acquisitionDate.withDayOfMonth(1),
                    dep.getCalculationDate());
            BigDecimal monthDep = (monthIndex == usefulLifeMonths - 1)
                    ? asset.getAcquisitionValue().subtract(correctAccumulated).subtract(residualValue)
                    : correctMonthlyDep;
            correctAccumulated = correctAccumulated.add(monthDep);
            registeredAccumulated = registeredAccumulated.add(dep.getPeriodDepreciation());
        }

        BigDecimal adjustmentAmount = correctAccumulated.subtract(registeredAccumulated);
        if (adjustmentAmount.compareTo(BigDecimal.ZERO) == 0
                || adjustmentAmount.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) == 0) {
            return Mono.empty();
        }

        Depreciation adjustment = new Depreciation();
        adjustment.setAssetId(asset.getId());
        adjustment.setInitialValue(asset.getAcquisitionValue());
        adjustment.setResidualValue(residualValue);
        adjustment.setUsefulLifeYears((int) Math.ceil(usefulLifeMonths / 12.0));
        adjustment.setAnnualDepreciation(correctMonthlyDep.multiply(BigDecimal.valueOf(12))
                .setScale(2, RoundingMode.HALF_UP));
        adjustment.setMonthlyDepreciation(correctMonthlyDep.setScale(2, RoundingMode.HALF_UP));
        adjustment.setPeriodDepreciation(adjustmentAmount.setScale(2, RoundingMode.HALF_UP));
        adjustment.setPreviousAccumulatedDepreciation(
                registeredAccumulated.setScale(2, RoundingMode.HALF_UP));
        adjustment.setCurrentAccumulatedDepreciation(
                correctAccumulated.setScale(2, RoundingMode.HALF_UP));
        adjustment.setPreviousBookValue(
                asset.getAcquisitionValue().subtract(registeredAccumulated).setScale(2, RoundingMode.HALF_UP));
        adjustment.setCurrentBookValue(
                asset.getAcquisitionValue().subtract(correctAccumulated).setScale(2, RoundingMode.HALF_UP));
        adjustment.setCalculationDate(LocalDateTime.now());
        adjustment.setFiscalYear(LocalDateTime.now().getYear());
        adjustment.setCalculationMonth(LocalDateTime.now().getMonthValue());
        adjustment.setCalculationStatus("ADJUSTED");
        adjustment.setCalculatedBy(SYSTEM_USER_ID);
        adjustment.setObservations("Ajuste por modificación de datos del activo. "
                + "Diferencia: " + adjustmentAmount.setScale(2, RoundingMode.HALF_UP));

        return persistencePort.save(adjustment)
                .then(updateAssetFromDepreciation(asset.getId(), convertToResponse(adjustment)))
                .then();
    }

    private Mono<Void> regenerateDepreciation(Asset asset) {
        LocalDateTime acquisitionDate = asset.getAcquisitionDate() != null
                ? asset.getAcquisitionDate().atStartOfDay()
                : LocalDateTime.now();
        int usefulLifeMonths = asset.getUsefulLife() != null ? asset.getUsefulLife() : 0;
        BigDecimal residualValue = asset.getResidualValue() != null ? asset.getResidualValue() : BigDecimal.ZERO;

        return generateAutomaticDepreciations(
                asset.getId(),
                asset.getAcquisitionValue(),
                residualValue,
                usefulLifeMonths,
                acquisitionDate)
                .last()
                .flatMap(lastDep -> updateAssetFromDepreciation(asset.getId(), lastDep))
                .then();
    }

    @Override
    public Mono<DepreciationResponse> create(DepreciationRequest request) {
        Depreciation depreciation = new Depreciation();
        BeanUtils.copyProperties(request, depreciation);

        // Depreciation calculation
        BigDecimal initialValue = depreciation.getInitialValue() != null
                ? depreciation.getInitialValue()
                : BigDecimal.ZERO;
        BigDecimal residualValue = depreciation.getResidualValue() != null
                ? depreciation.getResidualValue()
                : BigDecimal.ZERO;
        Integer usefulLife = depreciation.getUsefulLifeYears();

        if (initialValue.compareTo(BigDecimal.ZERO) > 0 && usefulLife != null && usefulLife > 0) {
            BigDecimal depreciableValue = initialValue.subtract(residualValue);
            depreciation.setAnnualDepreciation(
                    depreciableValue.divide(BigDecimal.valueOf(usefulLife), 2,
                            RoundingMode.HALF_UP));
            depreciation.setMonthlyDepreciation(
                    depreciation.getAnnualDepreciation().divide(BigDecimal.valueOf(12), 2,
                            RoundingMode.HALF_UP));
        } else {
            depreciation.setAnnualDepreciation(BigDecimal.ZERO);
            depreciation.setMonthlyDepreciation(BigDecimal.ZERO);
        }

        depreciation.setPreviousAccumulatedDepreciation(BigDecimal.ZERO);
        depreciation.setPeriodDepreciation(depreciation.getMonthlyDepreciation());
        depreciation.setCurrentAccumulatedDepreciation(depreciation.getPeriodDepreciation());
        depreciation.setPreviousBookValue(initialValue);
        depreciation.setCurrentBookValue(initialValue.subtract(depreciation.getPeriodDepreciation()));
        depreciation.setCalculationStatus("CALCULATED");
        depreciation.setCalculationDate(LocalDateTime.now());

        if (depreciation.getCalculatedBy() == null) {
            depreciation.setCalculatedBy(SYSTEM_USER_ID);
        }

        depreciation.setFiscalYear(LocalDateTime.now().getYear());

        return persistencePort.save(depreciation)
                .map(this::convertToResponse);
    }

    @Override
    public Flux<DepreciationResponse> generateAutomaticDepreciations(
            UUID assetId,
            BigDecimal initialValue,
            BigDecimal residualValue,
            int usefulLifeMonths,
            LocalDateTime acquisitionDate) {

        if (initialValue.compareTo(residualValue) <= 0 || usefulLifeMonths <= 0) {
            return Flux.empty();
        }

        LocalDateTime startMonth = acquisitionDate.withDayOfMonth(1);
        BigDecimal totalDepreciable = initialValue.subtract(residualValue);
        BigDecimal monthlyDepreciation = totalDepreciable.divide(
                BigDecimal.valueOf(usefulLifeMonths), 10, RoundingMode.HALF_UP);

        LocalDateTime now = LocalDateTime.now();
        int monthsSinceAcquisition = (int) java.time.temporal.ChronoUnit.MONTHS.between(
                startMonth, now.withDayOfMonth(1));

        int monthsToGenerate = Math.min(usefulLifeMonths, monthsSinceAcquisition + 1);

        return Flux.range(0, monthsToGenerate)
                .flatMap(monthIndex -> {
                    BigDecimal accumulatedDepPrev = monthlyDepreciation
                            .multiply(BigDecimal.valueOf(monthIndex));
                    BigDecimal monthDep = monthlyDepreciation;

                    if (monthIndex == usefulLifeMonths - 1) {
                        monthDep = initialValue.subtract(accumulatedDepPrev)
                                .subtract(residualValue);
                    }

                    BigDecimal accumulatedDepCurrent = accumulatedDepPrev.add(monthDep);
                    BigDecimal currentBookValue = initialValue.subtract(accumulatedDepCurrent);

                    Depreciation dep = new Depreciation();
                    dep.setAssetId(assetId);
                    dep.setInitialValue(initialValue);
                    dep.setResidualValue(residualValue);
                    dep.setUsefulLifeYears((int) Math.ceil(usefulLifeMonths / 12.0));
                    dep.setMonthlyDepreciation(monthDep.setScale(2, RoundingMode.HALF_UP));
                    dep.setAnnualDepreciation(monthDep.multiply(BigDecimal.valueOf(12)).setScale(2,
                            RoundingMode.HALF_UP));
                    dep.setPeriodDepreciation(monthDep.setScale(2, RoundingMode.HALF_UP));
                    dep.setPreviousAccumulatedDepreciation(
                            accumulatedDepPrev.setScale(2, RoundingMode.HALF_UP));
                    dep.setCurrentAccumulatedDepreciation(
                            accumulatedDepCurrent.setScale(2, RoundingMode.HALF_UP));
                    dep.setPreviousBookValue(initialValue.subtract(accumulatedDepPrev).setScale(2,
                            RoundingMode.HALF_UP));
                    dep.setCurrentBookValue(currentBookValue.setScale(2, RoundingMode.HALF_UP));
                    dep.setCalculationDate(startMonth.plusMonths(monthIndex));
                    dep.setFiscalYear(startMonth.plusMonths(monthIndex).getYear());
                    dep.setCalculationMonth(startMonth.plusMonths(monthIndex).getMonthValue());
                    dep.setCalculationStatus("CALCULATED");
                    dep.setCalculatedBy(SYSTEM_USER_ID);

                    return persistencePort.save(dep).map(this::convertToResponse);
                })
                .sort((d1, d2) -> d1.getCalculationDate().compareTo(d2.getCalculationDate()));
    }

    @Override
    public Mono<DepreciationResponse> getById(UUID id) {
        return persistencePort.findById(id)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new DepreciationNotFoundException(
                                "Depreciation not found with ID: " + id)));
    }

    @Override
    public Flux<DepreciationResponse> getAll() {
        return persistencePort.findAll()
                .map(this::convertToResponse);
    }

    @Override
    public Flux<DepreciationResponse> getByAssetId(UUID assetId) {
        return persistencePort.findByAssetId(assetId)
                .map(this::convertToResponse);
    }

    @Override
    public Flux<DepreciationResponse> getByFiscalYear(Integer fiscalYear) {
        return persistencePort.findByFiscalYear(fiscalYear)
                .map(this::convertToResponse);
    }

    @Override
    public Mono<DepreciationResponse> getByAssetAndPeriod(UUID assetId, Integer fiscalYear,
            Integer calculationMonth) {
        return persistencePort.findByAssetAndPeriod(assetId, fiscalYear, calculationMonth)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new DepreciationNotFoundException(
                                "Depreciation not found for asset: " + assetId +
                                        ", year: " + fiscalYear +
                                        ", month: " + calculationMonth)));
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new DepreciationNotFoundException(
                                "Depreciation not found with ID: " + id)))
                .flatMap(dep -> persistencePort.deleteById(id));
    }

    @Override
    public Mono<Void> deleteByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth) {
        return persistencePort.findByAssetId(assetId)
                .flatMap(dep -> persistencePort.deleteById(dep.getId()))
                .then();
    }

    @Override
    public Mono<DepreciationResponse> approve(UUID id, UUID approvedBy) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new DepreciationNotFoundException(
                                "Depreciation not found with ID: " + id)))
                .flatMap(depreciation -> {
                    depreciation.setCalculationStatus("APPROVED");
                    depreciation.setApprovedBy(approvedBy);
                    depreciation.setApprovalDate(LocalDateTime.now());
                    return persistencePort.save(depreciation);
                })
                .map(this::convertToResponse);
    }

    private Mono<DepreciationResponse> updateAssetFromDepreciation(UUID assetId, DepreciationResponse dep) {
        return assetPersistencePort.findById(assetId)
                .flatMap(asset -> {
                    asset.setAccumulatedDepreciation(dep.getCurrentAccumulatedDepreciation());
                    asset.setCurrentValue(dep.getCurrentBookValue());
                    return assetPersistencePort.save(asset).thenReturn(dep);
                });
    }

    private DepreciationResponse convertToResponse(Depreciation depreciation) {
        DepreciationResponse response = new DepreciationResponse();
        BeanUtils.copyProperties(depreciation, response);
        return response;
    }
}
