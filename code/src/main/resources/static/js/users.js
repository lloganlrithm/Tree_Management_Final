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
// U4: เปลี่ยน select/checkbox ที่มี data-autosubmit แล้วส่งฟอร์มทันที (หน้า admin จัดการผู้ใช้)
document.querySelectorAll("[data-autosubmit]").forEach(function (el) {
  el.addEventListener("change", function () { el.form.submit(); });
});

// U4: แสดงชื่อไฟล์ + พรีวิวรูปที่เลือก (input file ซ่อนอยู่ในกรอบ .drop)
document.querySelectorAll('input[type="file"]').forEach(function (input) {
  var label = input.closest("label");
  var out = label && label.querySelector("[data-file-name]");
  if (!out) return;
  var preview = label.querySelector("[data-file-preview]");
  var avatar = input.dataset.previewTarget && document.querySelector(input.dataset.previewTarget);
  var avatarSrc = avatar && avatar.getAttribute("src");
  input.addEventListener("change", function () {
    var file = input.files[0];
    out.textContent = file ? "เลือกแล้ว: " + file.name : "";
    var url = file ? URL.createObjectURL(file) : null;
    if (preview) { preview.hidden = !file; if (url) preview.src = url; }
    if (avatar) avatar.src = url || avatarSrc;
  });
});
