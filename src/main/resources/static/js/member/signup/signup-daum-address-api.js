const addressCheckBtn = document.querySelector('#address-check-btn');

addressCheckBtn.addEventListener('click', () => {
    new daum.Postcode({
        oncomplete: function(data) {
            let address = data.userSelectedType == 'R' ? data.address : data.roadAddress;
            console.log('address: ' + address)
            document.querySelector('#address').value = address;
        }
    }).open();
});