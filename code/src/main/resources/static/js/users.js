// E3: ตรวจฟอร์มสมัครสมาชิกในหน้า (ฝั่ง server ต้องตรวจซ้ำด้วย @Valid)

// ปุ่มแสดง/ซ่อนรหัสผ่าน: <button data-toggle-pw="id ของ input">
document.querySelectorAll("[data-toggle-pw]").forEach(function (btn) {
  btn.addEventListener("click", function () {
    var input = document.getElementById(btn.dataset.togglePw);
    var show = input.type === "password";
    input.type = show ? "text" : "password";
    btn.textContent = show ? "ซ่อน" : "แสดง";
  });
});

var form = document.getElementById("signup-form");
if (form) {
  var EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  function showError(id, msg) {
    var input = document.getElementById(id);
    var box = form.querySelector('[data-error-for="' + id + '"]');
    input.classList.toggle("is-invalid", !!msg);
    box.textContent = msg || "";
    box.hidden = !msg;
    return !msg;
  }

  function val(id) { return document.getElementById(id).value.trim(); }

  var rules = {
    "g-first": function () { return val("g-first") ? "" : "กรุณากรอกชื่อ"; },
    "g-last":  function () { return val("g-last") ? "" : "กรุณากรอกนามสกุล"; },
    "g-email": function () { return EMAIL.test(val("g-email")) ? "" : "รูปแบบอีเมลไม่ถูกต้อง"; },
    "g-pw":    function () { return document.getElementById("g-pw").value.length >= 8 ? "" : "รหัสผ่านต้องมีอย่างน้อย 8 ตัวอักษร"; },
    "g-pw2":   function () { return document.getElementById("g-pw2").value === document.getElementById("g-pw").value ? "" : "รหัสผ่านไม่ตรงกัน"; }
  };

  // ตรวจทันทีเมื่อออกจากช่อง
  Object.keys(rules).forEach(function (id) {
    document.getElementById(id).addEventListener("blur", function () { showError(id, rules[id]()); });
  });

  // ตรวจทุกช่องก่อนส่ง ถ้ามีช่องไม่ผ่าน ไม่ส่งฟอร์ม
  form.addEventListener("submit", function (e) {
    var ok = Object.keys(rules).map(function (id) { return showError(id, rules[id]()); }).every(Boolean);
    if (!ok) e.preventDefault();
  });
}

// E4: พรีวิวรูปโปรไฟล์จาก URL: <input data-preview="#id ของ <img>">
document.querySelectorAll("[data-preview]").forEach(function (input) {
  var img = document.querySelector(input.dataset.preview);
  if (!img) return;
  var fallback = img.getAttribute("src");
  input.addEventListener("input", function () {
    img.src = input.value.trim() || fallback;
  });
  img.addEventListener("error", function () { img.src = fallback; });
});