package com.morningstar.old.infra.handler;

import com.morningstar.old.infra.exception.BaseException;
import com.morningstar.old.infra.response.R;
import com.morningstar.old.infra.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;


@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private void logExceptionChain(Throwable throwable, int depth) {
        if (throwable == null || depth > 7) return;
        log.error("{}{}:{}", String.join("", Collections.nCopies(depth * 4, "-")),
                throwable.getClass().getSimpleName(),
                throwable.getMessage());
        if (throwable.getCause() != null) {
            logExceptionChain(throwable.getCause(), depth + 1);
        }
    }

    /**
     * 捕获未知异常
     */
    @ExceptionHandler
    public R<Object> unknownExceptionHandler(Exception ex) {
        log.error("捕获到未知异常: ", ex);
        log.error("快速定位根因: ");
        logExceptionChain(ex, 0);
        return R.error("服务器内部异常，请稍后重试");
    }

    /**
     * 捕捉业务异常
     */
    @ExceptionHandler
    public R<Object> baseExceptionHandler(BaseException ex) {
        log.error("业务异常信息: {}", ex.getMessage());
        return R.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 捕捉 @RequestParam 或 @PathVariable 字段校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Object> handleValidationExceptions(ConstraintViolationException ex) {
        // 提取所有校验失败的提示信息，并用分号拼接
        String errorMessage = ex.getConstraintViolations().stream()
                // 如果你想带上具体的参数名，可以用 violation.getPropertyPath() + ": " + violation.getMessage()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        return R.error(HttpStatus.BAD_REQUEST.name(), "请求参数错误: " + errorMessage);
    }

    /**
     * 捕捉requestBody字段校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Object> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        String errorMessage = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining("; "));
        return R.error(HttpStatus.BAD_REQUEST.name(), errorMessage);
    }

    /**
     * 捕捉requestBody反序列化异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Object> handleJsonParseError(HttpMessageNotReadableException ex) {
        return R.error(HttpStatus.BAD_REQUEST.name(), "请求体解析失败: " + Objects.requireNonNull(ex.getRootCause()).getMessage());
    }

    /**
     * 捕捉邮件发送错误
     */
    @ExceptionHandler({MailSendException.class, MailParseException.class})
    public R<Object> mailSendExceptionHandler() {
        return R.error("邮件发送出错");
    }

    /**
     * Multipart文件过大
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Object> multipartFileSizeExceededExceptionHandler() {
        return R.error("Multipart文件过大");
    }

    /**
     * 捕捉 405 错误
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Object> handle405(HttpRequestMethodNotSupportedException ex) {
        return R.error(HttpStatus.METHOD_NOT_ALLOWED.name(), "请求方法错误，支持的方法有: " + Arrays.toString(ex.getSupportedMethods()));
    }

    /**
     * 捕捉其他 400 错误
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestPartException.class, MultipartException.class})
    public R<Object> handle400(Exception ex) {
        return R.error(HttpStatus.BAD_REQUEST.name(), "请求参数错误: " + ex.getMessage());
    }

    /**
     * 捕捉 404 错误
     */
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public R<Object> handle404(Exception ex) {
        return R.error(HttpStatus.NOT_FOUND.name(), "资源不存在: " + ex.getMessage());
    }

    /**
     * 捕捉 403 错误
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Object> handle403(AccessDeniedException e) {
        // 打印日志，方便排查
        log.warn("权限不足被拦截: {}", e.getMessage());
        // 直接复用你之前写的返回逻辑
        return R.error(ResponseCode.NO_PERMISSION);
    }
}
