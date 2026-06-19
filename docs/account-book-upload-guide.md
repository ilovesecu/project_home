# 가계부 거래내역 업로드 기능 3 기틀

## 1. MySQL DDL

```sql
CREATE TABLE home_project.transaction_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래내역 ID',
    transaction_at DATETIME NOT NULL COMMENT '거래 일시',
    description VARCHAR(255) NULL COMMENT '적요',
    transaction_type VARCHAR(50) NULL COMMENT '거래 유형',
    transaction_institution VARCHAR(100) NULL COMMENT '거래 기관',
    account_number VARCHAR(64) NULL COMMENT '계좌번호',
    amount BIGINT NOT NULL COMMENT '거래 금액: 입금 양수, 출금 음수',
    balance_after BIGINT NULL COMMENT '거래 후 잔액',
    memo VARCHAR(500) NULL COMMENT '메모',
    source_file_name VARCHAR(255) NULL COMMENT '업로드 원본 파일명',
    source_row_number INT NULL COMMENT '원본 파일 행 번호',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    PRIMARY KEY (id),
    INDEX idx_transaction_history_transaction_at (transaction_at),
    INDEX idx_transaction_history_account_number (account_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='가계부 거래내역';
```

## 2. API

- `POST /api/account-book/transactions/upload`
- `Content-Type: multipart/form-data`
- form field name: `file`
- 지원 파일: `.csv`, `.xls`, `.xlsx`
- 토스뱅크 형식 기준으로 1~8번째 줄은 메타데이터, 9번째 줄은 헤더로 간주해 제외하고 10번째 줄부터 저장한다.

## 3. build.gradle 의존성

```gradle
implementation 'com.opencsv:opencsv:5.9'
implementation 'org.apache.poi:poi-ooxml:5.2.5'
```

## 4. React 업로드 컴포넌트 예시

```jsx
import { useState } from "react";

export default function TransactionHistoryUploader() {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const handleUpload = async () => {
    if (!file) {
      setError("업로드할 파일을 선택해 주세요.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    setUploading(true);
    setError("");
    setResult(null);

    try {
      const response = await fetch("/api/account-book/transactions/upload", {
        method: "POST",
        body: formData,
      });

      const body = await response.json();
      if (!response.ok || body.status !== "SUCCESS") {
        throw new Error(body.message || "거래내역 업로드에 실패했습니다.");
      }

      setResult(body.data);
    } catch (e) {
      setError(e.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <section>
      <input
        type="file"
        accept=".csv,.xls,.xlsx"
        onChange={(event) => setFile(event.target.files?.[0] ?? null)}
      />
      <button type="button" onClick={handleUpload} disabled={!file || uploading}>
        {uploading ? "업로드 중" : "업로드"}
      </button>

      {error && <p>{error}</p>}

      {result && (
        <div>
          <p>파싱 성공: {result.parsedCount}건</p>
          <p>DB 저장: {result.insertedCount}건</p>
          <p>파싱 실패: {result.failedCount}건</p>
          {result.errors?.length > 0 && (
            <ul>
              {result.errors.map((item) => (
                <li key={`${item.rowNumber}-${item.message}`}>
                  {item.rowNumber}행: {item.message}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </section>
  );
}
```

운영 프론트엔드에서 JWT 인증을 쓰는 화면이라면 `fetch` 호출에 `Authorization: Bearer ...` 헤더를 추가하면 된다.

## 5. 백엔드 구현 위치

- 컨트롤러: `com.ilovepc.project_home.web.accountbook.controller.TransactionHistoryController`
- 서비스/파서: `com.ilovepc.project_home.web.accountbook.service.TransactionHistoryUploadService`
- MyBatis 인터페이스: `com.ilovepc.project_home.repository.TransactionHistoryMapper`
- XML mapper: `src/main/resources/mapper/TransactionHistoryMapper.xml`

파싱 중 날짜 형식 오류, 금액 형식 오류, 컬럼 부족, 빈 줄은 전체 업로드를 중단하지 않는다. 문제 행은 로그에 남기고 다음 행 처리를 계속하며, 응답에는 최대 50개 오류만 포함한다.
