async function apiErrorResponse(err, callbackRenderErrorMessage) {
    try {
        const error = await err.json();
        console.log(error);
        const code = error?.code ?? 'UNKNOWN_ERROR';

        if (code === 'UNKNOWN_ERROR') throw new Error();
        else if (code === 'VALIDATION_ERROR') callbackRenderErrorMessage(error.errors);
        else alert(error.message);
    } catch(e) {
        console.error(e);
        alert('알 수 없는 오류가 발생했습니다');
    }
}