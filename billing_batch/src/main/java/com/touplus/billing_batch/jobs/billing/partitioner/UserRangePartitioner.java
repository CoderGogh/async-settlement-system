package com.touplus.billing_batch.jobs.billing.partitioner;

import com.touplus.billing_batch.domain.dto.MinMaxIdDto;
import com.touplus.billing_batch.domain.repository.BillingUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserRangePartitioner implements Partitioner {

    private final BillingUserRepository billingUserRepository;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        // 1. DB에서 전체 ID 범위 조회 (500만 건 기준)
        MinMaxIdDto minMax = billingUserRepository.findMinMaxId();

        if (minMax == null ||
                minMax.getMinId() == null ||
                minMax.getMaxId() == null) {

            return Map.of();
        }

        long min = minMax.getMinId();
        long max = minMax.getMaxId();

        // 2. 한 파티션당 처리할 데이터 크기 계산
        long total = max - min + 1;


        if (total <= 0) {
            return Map.of();
        }

        int actualGridSize = (int) Math.min(gridSize, total);
        long targetSize = (total + actualGridSize - 1) / actualGridSize;

        Map<String, ExecutionContext> result = new HashMap<>();
        long start = min;
        long end = start + targetSize - 1;

        // 3. gridSize만큼 범위를 나누어 ExecutionContext 생성
        for (int i = 0; i < actualGridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.putLong("minValue", start);
            context.putLong("maxValue", Math.min(end, max));
//            context.putLong("minValue", 900000000L);
//            context.putLong("maxValue", 900000000L);

            result.put("partition" + i, context);

            start += targetSize;
            end += targetSize;
        }

        return result;
    }
}
