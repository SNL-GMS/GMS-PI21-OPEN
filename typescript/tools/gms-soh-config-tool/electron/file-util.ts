export const buildBlob = (data: any) => {
  return new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
}

export const getFileFromData = (data: any, fileName: string) => {
  return new File([buildBlob(data)], fileName, { type: 'application/json '})
}