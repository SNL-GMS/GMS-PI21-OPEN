import { buildBlob } from "../shared/file-util";

function download(fileUrl: string, fileName: string) {
  var a = document.createElement("a");
  a.href = fileUrl;
  a.setAttribute("download", fileName);
  a.click();
}

export const exportData = (data: any, fileName: string) => {
  const configURL = URL.createObjectURL(buildBlob(data));
  download(configURL, fileName);
}
